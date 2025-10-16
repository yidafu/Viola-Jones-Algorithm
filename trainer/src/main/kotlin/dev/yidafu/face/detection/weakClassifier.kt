package dev.yidafu.face.detection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.*

data class ThresholdPolarity(
    val threshold: Float,
    val polarity: Int,
)

data class ClassifierResult(
    val threshold: Float,
    val polarity: Int,
    val classificationError: Float,
    val classifier: Feature,
)

data class WeakClassifier(
    val threshold: Float,
    val polarity: Int,
    val alpha: Float,
    val classifier: Feature,
)


fun normalizeWeights(ws: List<Float>): List<Float> {
    val sum = ws.sum()
    
    // 防止除以 0 或 NaN
    if (sum.isNaN() || sum.isInfinite() || sum <= 0f) {
        println("ERROR: Invalid weight sum: $sum")
        println("First 10 weights: ${ws.take(10)}")
        // 返回均匀分布作为后备方案
        val uniformWeight = 1.0f / ws.size
        return List(ws.size) { uniformWeight }
    }
    
    return ws.map { it / sum }
}

/**
 * 构建累积和数组，用于计算ROC曲线相关的统计数据
 *
 * @param ys 标签列表，其中小于等于0.5f的值被视为负类，大于0.5f的值被视为正类
 * @param ws 权重列表，与标签列表一一对应，用于加权计算累积和
 *
 * @return 包含两组数据的Pair：
 *         第一组(Pair<Float, MutableList<Float>>)：负类的总累积权重和累积过程列表
 *         第二组(Pair<Float, MutableList<Float>>)：正类的总累积权重和累积过程列表
 */
fun buildRunningSums(
    ys: List<Float>,
    ws: List<Float>,
): Pair<Pair<Float, MutableList<Float>>, Pair<Float, MutableList<Float>>> {
    // 初始化变量
    val sMinuses = mutableListOf<Float>() // 累积负类计数列表
    val sPluses = mutableListOf<Float>()  // 累积正类计数列表
    var sMinus = 0f // 当前累积负类计数
    var sPlus = 0f  // 当前累积正类计数

    // 遍历数据，根据标签值将权重累加到对应的类别中，并记录累积过程
    for ((y, w) in ys.zip(ws)) {
        if (y <= 0.5f) {
            sMinus += w
        } else {
            sPlus += w
        }

        // 添加当前累积值到列表
        sMinuses.add(sMinus)
        sPluses.add(sPlus)
    }
    return Pair(
        Pair(sMinus, sMinuses),
        Pair(sPlus, sPluses)
    )
}

/**
 * 寻找最佳阈值函数
 *
 * 该函数通过遍历所有可能的阈值点，计算每个阈值点对应的分类错误率，
 * 找到使错误率最小的最佳阈值及其极性。
 *
 * @param zs 阈值候选点列表
 * @param totalMinus 负类样本的总权重
 * @param totalPlus 正类样本的总权重
 * @param sMinuses 累积负类权重列表，与zs对应位置对应
 * @param sPluses 累积正类权重列表，与zs对应位置对应
 * @return ThresholdPolarity 包含最佳阈值和极性的数据类
 */
fun findBestThreshold(
    zs: List<Float>,
    totalMinus: Float,
    totalPlus: Float,
    sMinuses: List<Float>,
    sPluses: List<Float>,
): ThresholdPolarity {
    var minE = Float.MAX_VALUE
    var minZ = 0f
    var polarity = 0

    // 遍历所有阈值候选点，计算对应的分类错误率
    for ((z, sMinus, sPlus) in zs.zip(sMinuses, sPluses)) {
        /**
         *  当前位置，正类样本权重 + 错误分类的负类样本权重
         *  即： 正确分类的负类样本权重 + (负类样本总权重 - 当前位置之前负类样本权重)
         *
         *  我们希望：
         *  正确分类的负类样本权重 ==> 最多
         *  错误分类的负类样本权重 ==> 最少
         */
        val error1 = sPlus + (totalMinus - sMinus)

        /**
         * 当前位置，负类样本权重 + 错误分类的正类样本权重
         * 即：正确分类的负类样本权重 + (正类样本总权重 - 当前位置之前正样本权重)
         *
         *  我们希望：
         * 正确分类的负类样本权重 ==> 最多
         * 错误分类的正类样本权重 ==> 最少
         */
        val error2 = sMinus + (totalPlus - sPlus)

        // 更新最佳分类效果和对应的最佳阈值
        if (error1 < minE) {
            minE = error1
            minZ = z
            polarity = -1
        } else if (error2 < minE) {
            minE = error2
            minZ = z
            polarity = 1
        }
    }
    // 在 minZ 上，正类样本的总权重和负类样本的总权重差值最小
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
    val (tMinus, sMinuses) = minus
    val (tPlus, sPluses) = plus
    return findBestThreshold(
        zsOrder,
        tMinus,
        tPlus,
        sMinuses,
        sPluses
    )
}

/**
 * 弱分类器函数，根据特征值与阈值的比较结果进行分类
 *
 * @param x 输入的二维数组数据
 * @param f 特征对象，用于计算特征值
 * @param polarity 极性参数，控制分类方向
 * @param theta 阈值参数，用于比较判断
 * @return 分类结果，返回0或1 0--负类 1--正类
 */
fun weekClassifier(x: D2Array<Float>, f: Feature, polarity: Int, theta: Float): Int {
    /**
     * 通过极性、阈值与特征值的差值符号来计算分类结果
     * 先计算polarity * (theta - f.sum(x))的符号值(-1或1)
     * 然后通过符号值加1再除以2，将结果映射到0或1
     */
    return (polarity * (theta - f.sum(x)).sign.toInt() + 1) / 2
}

fun runWeekClassifier(x: D2Array<Float>, c: WeakClassifier): Int {
    return weekClassifier(x, c.classifier, c.polarity, c.threshold)
}

fun applyFeature(
    f: Feature,
    xis: List<D2Array<Float>>,
    ys: List<Float>,
    ws: List<Float>,
): ClassifierResult {
    // 计算特征区域的特征值
    val zs = xis.map {
        f.sum(it)
    }
    // 找到最优预制和极性
    val result = determineThresholdPolarity(zs, ws, ys)
    var classificationError = 0f
    for ((x, y, w) in xis.zip(ys, ws)) {
        val h = weekClassifier(x, f, result.polarity, result.threshold)
        // h y 相等 说明预测正确，否则分类错误
    classificationError += w * abs(h - y)
    }
    return ClassifierResult(
        result.threshold,
        result.polarity,
        classificationError,
        f,
    )
}

suspend fun buildWeakClassifiers(
    prefix: String,
    round: Int,
    xis: List<D2Array<Float>>,
    ys: List<Float>,
    features: List<Feature>,
    ws: List<Float>? = null,
): Pair<MutableList<out Any>, MutableList<List<Float>>> = withContext(Dispatchers.Default) {
    var locWs: MutableList<Float> = if (ws == null) {
        val m = ys.count { it < .5 }
        val l = ys.count { it >= .5 }

        ys.map { y ->
            if (y < .5) {
                1.0f / (2.0f * m)
            } else {
                1.0f / (2.0f * l)
            }
        }
    } else {
        ws
    }.toMutableList()

    val wHistory = mutableListOf<List<Float>>(locWs)

    val totalStartTime = System.currentTimeMillis()
    val weakClassifiers = mutableListOf<WeakClassifier>()

    // 按轮次训练弱分类器
    for (t in 0..<round) {
        // 目标：每一轮都找出最好的弱分类器
        println("\nBuilding weak classifier ${t + 1}/${round} ...")
        val startTime = System.currentTimeMillis()
        locWs = normalizeWeights(locWs).toMutableList()
        // 使用协程并行化特征评估
        val chunkSize = 1500  // 每块 1500 个特征
        val processedCount = AtomicInteger(0)
        val bestResult = AtomicReference(ClassifierResult(
            polarity = 0,
            threshold = 0f,
            classificationError = Float.MAX_VALUE,
            classifier = Feature.Empty
        ))
        
        // 需要每个特征对所有图片样本进行计算
        val allResults = features.chunked(chunkSize).mapIndexed { chunkIdx, chunk ->
            async(Dispatchers.Default) {
                chunk.mapIndexed { localIdx, f ->
                    val globalIdx = chunkIdx * chunkSize + localIdx
                    // 单个特征计算所有图片样本里的错误率
                    val result = applyFeature(f, xis, ys, locWs)
                    
                    // 原子性更新最佳结果
                    var currentBest: ClassifierResult
                    do {
                        currentBest = bestResult.get()
                        if (result.classificationError >= currentBest.classificationError) {
                            break
                        }
                    } while (!bestResult.compareAndSet(currentBest, result))
                    
                    result
                }
            }
        }.awaitAll().flatten()
        
        val best = bestResult.get()

        // 防止分类错误率为极端值（0.0 或 1.0）导致 NaN
        val epsilon = 1e-10f
        val clippedError = best.classificationError.coerceIn(epsilon, 1f - epsilon)
        
        // 检查分类错误率是否过高（>=0.5 表示比随机猜测还差）
        if (clippedError >= 0.5f) {
            println("WARNING: Classification error ${best.classificationError} >= 0.5, weak learner is worse than random guessing!")
            println("Stopping training early at round ${t + 1}/${round}")
            break
        }
        
        val beta = clippedError / (1f - clippedError)
        var alpha = ln(1f / beta)
        
        // 对于接近完美的分类器，alpha 可能会非常大，我们需要限制其上限
        val maxAlpha = 25f  // 设置合理的上限
        if (alpha.isInfinite() || alpha > maxAlpha) {
            println("Perfect or near-perfect classifier found! Classification error: ${best.classificationError}")
            alpha = maxAlpha
        }
        
        // 检查 beta 和 alpha 是否有效
        if (beta.isNaN() || alpha.isNaN()) {
            println("ERROR: Invalid beta ($beta) or alpha ($alpha) detected!")
            println("Classification error: ${best.classificationError}, Clipped error: $clippedError")
            break
        }

        val classifier = WeakClassifier(
            threshold = best.threshold,
            polarity = best.polarity,
            classifier = best.classifier,
            alpha = alpha
        )
        
        // 更新权重
        for ((i, pair) in xis.zip(ys).withIndex()) {
            val (x, y) = pair
            val h = runWeekClassifier(x, classifier)
            val e = abs(h - y)
            locWs[i] = locWs[i] * beta.toDouble().pow((1 - e).toDouble()).toFloat()
        }
        
        // 检查权重更新后是否出现 NaN 或 Inf
        if (locWs.any { it.isNaN() || it.isInfinite() }) {
            println("ERROR: NaN or Infinite weights detected after update!")
            println("Beta: $beta, Alpha: $alpha")
            println("First 10 weights: ${locWs.take(10)}")
            break
        }
        
        weakClassifiers.add(classifier)
        wHistory.add(locWs.toList())
        
        // 输出本轮训练结果
        val roundEndTime = System.currentTimeMillis()
        val roundDuration = (roundEndTime - startTime) / 1000.0
        val totalDuration = (roundEndTime - totalStartTime) / 1000.0
        println("  → Best classifier: ${best.classifier}")
        println("  → Classification error: ${String.format("%.6f", best.classificationError)}")
        println("  → Alpha: ${String.format("%.4f", alpha)}, Beta: ${String.format("%.6f", beta)}")
        println("  → Time: ${String.format("%.2f", roundDuration)}s (Total: ${String.format("%.2f", totalDuration)}s)")
        println("=".repeat(80))
    }
//        saveClassifier(classifier, prefix, t, featureNum)

    println("Done building $round weak classifiers.")


    weakClassifiers to wHistory
}
