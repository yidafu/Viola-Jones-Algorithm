package dev.yidafu.face.dectetion

import dev.yidafu.face.detection.reorder
import dev.yidafu.face.detection.argsort
import dev.yidafu.face.detection.zip
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sign

data class ThresholdPolarity(
    val threshold: Float,
    val polarity: Float,
)
data class ClassifierResult(
    val threshold: Float,
    val polarity: Float,
    val classificationError: Float,
    val classifier: Feature,
)
data class WeakClassifier(
    val threshold: Float,
    val polarity: Float,
    val alpha: Float,
    val classifier: Feature,
)


fun normalizeWeights(ws: List<Float>): List<Float> {
    val sum = ws.sum()
    return ws.map { it / sum }
}



fun buildRunningSums(ys: List<Float>, ws: List<Float>): Pair<Pair<Int, MutableList<Int>>, Pair<Int, MutableList<Int>>> {
    // 初始化变量
    val sMinuses = mutableListOf<Int>() // 累积负类计数列表
    val sPluses = mutableListOf<Int>()  // 累积正类计数列表
    var sMinus = 0 // 当前累积负类计数
    var sPlus = 0  // 当前累积正类计数
    var tMinus = 0 // 总负类计数（未在循环中使用）
    var tPlus = 0  // 总正类计数（未在循环中使用）

    // 遍历数据
    for ((z, y) in ys.zip(ws)) {
        if (y <= 0.5f) {
            sMinus += 1
            tMinus += 1
        } else {
            sPlus += 1
            tPlus += 1
        }

        // 添加当前累积值到列表
        sMinuses.add(sMinus)
        sPluses.add(sPlus)
    }
    return Pair(
        Pair(tMinus, sMinuses),
        Pair(tPlus, sPluses)
    )
}

fun findBestThreshold(
    zs: List<Float>,
    tMinus: Float,
    tPlus: Float,
    sMinuses: List<Float>,
    sPluses: List<Float>
) : ThresholdPolarity {
    var minE = Float.MIN_VALUE
    var minZ = 0f
    var polarity = 0f

    for ((z, sm, sp) in  zs.zip(sMinuses,sPluses) ) {
        val error1 = sp + (tMinus - sm)
        val error2 = sm + (tPlus - sp)
        if (error1 < minE) {
            minE = error1
            minZ = z
            polarity = -1f
        } else
            if (error2 < minE) {
                minE = error2
                minZ = z
                polarity = 1f
            }
    }
    return ThresholdPolarity(minZ, polarity)
}


fun determineThresholdPolarity(
    zs: List<Float>,
    ws: List<Float>,
    ys: List<Float>,
): ThresholdPolarity {
    val (zsOrder, orderIndex) = zs.argsort()
    val wsOrder = ws.reorder(orderIndex)
    val ysOrder = ys.reorder(orderIndex)

    val (minus, plus) = buildRunningSums(ysOrder, wsOrder)
    val (tMinus,  sMinuses) = minus;
    val (tPlus, sPluses) = plus
    return findBestThreshold(
        zsOrder,
        tMinus.toFloat(),
        tPlus.toFloat(),
        sMinuses.map { it.toFloat() },
        sPluses.map { it.toFloat() }
    )
}
fun weekClassifier(x: D2Array<Float>,f: Feature, polarity: Float, theta: Float): Float {
    return floor((((polarity * theta) - (polarity * f.sum(x))).sign + 1) / 2)
}
fun runWeekClassifier(x: D2Array<Float>, c: WeakClassifier): Float {
    return weekClassifier(x,  c.classifier, c.polarity, c.threshold,)
}

fun applyFeature(
    f: Feature,
    xis: List<D2Array<Float>>,
    ys: List<Float>,
    ws: List<Float>,
): ClassifierResult {
    val zs = xis.map {
        f.sum(it)
    }
    val result = determineThresholdPolarity(ys, ws, zs)
    var classificationError = 0f;
    for ((x,y,w) in xis.zip(ys, ws)) {
        val h = weekClassifier(x, f, result.polarity, result.threshold)
        classificationError += w * abs(h - y)
    }
    return ClassifierResult(
        result.threshold,
        result.polarity,
        classificationError,
        f,
    )
}

fun buildWeakClassifiers(
    prefix: String,
    featureNum: Int,
    xis: List<D2Array<Float>>,
    ys: List<Float>,
    features: List<Feature>,
    ws: List<Float>? = null,
) : Pair<MutableList<out Any>, MutableList<List<Float>>> {
    var locWs: MutableList<Float> = if (ws == null) {
        val m = ys.count { it < .5 }
        val l = ys.count { it >= .5}

        ys.map {  y ->
            if (y < .5) {
                1.0f/(2.0f*m)
            } else {
                1.0f/(2.0f*l)
            }
        }
    } else {
        ws
    }.toMutableList()

    val wHistory = mutableListOf<List<Float>>(locWs)

    val totalStartTime = System.currentTimeMillis()
    val weakClassifiers = mutableListOf<WeakClassifier>()
    for (t in 0..<featureNum) {
        println("Building weak classifier ${t+1}/${featureNum} ...")
        val startTime =  System.currentTimeMillis()
        locWs = normalizeWeights(locWs).toMutableList()
        var best = ClassifierResult(
            polarity=0f,
            threshold=0f,
            classificationError=Float.MAX_VALUE,
            classifier= Feature.Empty
        )
        features.forEachIndexed { i, f ->

            var improved = false

            val result = applyFeature(f, xis, ys, locWs)
            if (result.classificationError < best.classificationError) {
                improved = true
                best = result
            }

            if (improved) {
                val currentTime = System.currentTimeMillis()
                val totalDuration = currentTime - totalStartTime
                val duration = currentTime - startTime
                println("t=${t+1}/${featureNum} ${totalDuration/ 1000}s (${duration / 1000}s in this stage) ${i+1}/${features.size} ${100*i/features.size}% evaluated. Classification error improved to ${best.classificationError} using ${best.classifier} ...")
            }

        }

        val beta = best.classificationError / (1 - best.classificationError)
        val alpha = ln( 1f / beta);

        val classifier = WeakClassifier(threshold=best.threshold, polarity=best.polarity, classifier=best.classifier, alpha=alpha)
        for((i, pair) in xis.zip(ys).withIndex()) {
            val (x, y) = pair
            val h = runWeekClassifier(x,  classifier)
            val e = abs(h - y)
            locWs[i] = locWs[i] * beta.toDouble().pow((1 - e).toDouble()).toFloat()
        }
        weakClassifiers.add(classifier)
        wHistory.add(locWs.toList())
//        saveClassifier(classifier, prefix, t, featureNum)
    }

    println("Done building ${featureNum} weak classifiers.")

    return weakClassifiers to wHistory
}
