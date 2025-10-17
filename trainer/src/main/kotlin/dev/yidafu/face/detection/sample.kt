package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import java.nio.file.Path

fun sampleData(f: Int,
               b: Int,
               faceImages: List<Path>,
               bgImages: List<Path>
): Pair<List<D2Array<Int>>, D1Array<Float>> {
    val xs = mutableListOf<D2Array<Int>>()
    faceImages.sample(f).forEach {
        xs.add(openFace(it, true).toD2Array())
    }
    bgImages.sample(b).forEach {
        xs.add(openBackground(it, true).toD2Array())
    }
    val ones: D1Array<Float> = mk.ones<Float>(f, )  // 创建 p 个 1.0
    val zeros: D1Array<Float> = mk.zeros<Float>(b) // 创建 n 个 0.0
    val ys: D1Array<Float> = ones.cat(zeros)

    return Pair(xs, ys)
}


fun normalize(im: List<D2Array<Float>>, mean: Float , std: Float): List<D2Array<Float>> {
    return  im.map { i ->
        (i - mean) / std
    }
}


data class NormalizationParams(
    val mean: Float,
    val std: Float
)

fun sampleDataNormalized(
    p: Int,
    n: Int,
    faceImages: List<Path>,
    bgImages: List<Path>,
): Triple<List<D2Array<Float>>, D1Array<Float>, NormalizationParams> {
    // 验证样本数量
    if (faceImages.size < p) {
        println("WARNING: Requested $p face samples but only ${faceImages.size} available")
    }
    if (bgImages.size < n) {
        println("WARNING: Requested $n background samples but only ${bgImages.size} available")
    }
    
    val actualP = minOf(p, faceImages.size)
    val actualN = minOf(n, bgImages.size)
    
    val (xs, ys) = sampleData(actualP, actualN, faceImages, bgImages)
    
    // 简化检查，避免类型问题
    val sampleMean = xs.mean()
    val sampleStd = xs.std()
    
    // 防止除以0
    val safeStd = if (sampleStd < 1e-6f) {
        println("WARNING: Sample std is too small ($sampleStd), using 1.0 instead")
        1.0f
    } else {
        sampleStd
    }
    
    val floatXS = xs.map { it.asType<Float>() }
    val newXs = normalize(floatXS, sampleMean, safeStd)
    
    println("Sample statistics: mean=${String.format("%.4f", sampleMean)}, std=${String.format("%.4f", safeStd)}")
    println("Positive samples: $actualP, Negative samples: $actualN")
    
    val normParams = NormalizationParams(sampleMean, safeStd)
    
    return Triple(newXs, ys, normParams)
}