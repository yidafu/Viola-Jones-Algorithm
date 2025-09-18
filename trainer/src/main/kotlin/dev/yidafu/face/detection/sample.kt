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


fun sampleDataNormalized(
    p: Int,
    n: Int,
    faceImages: List<Path>,
    bgImages: List<Path>,
): Pair<List<D2Array<Float>>, D1Array<Float>> {
    val (xs, ys) = sampleData(p, n, faceImages, bgImages)
    val sampleMean = xs.mean()
    val sampleStd = xs.std()
    val floatXS = xs.map { it.asType<Float>() }
    val newXs = normalize(floatXS, sampleMean, sampleStd)
    return Pair(newXs, ys)
}