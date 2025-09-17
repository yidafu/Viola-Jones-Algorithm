package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.default.stat.DefaultStatistics
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import java.awt.image.BufferedImage
import kotlin.math.pow
import kotlin.math.sqrt


inline fun <T: Number> T.between(min: T, max: T): T {

    if ( this.compareTo(min) < 0) {
        return  min
    }
     if (this.compareTo(max) > 0) {
        return  max
    }
    return this
}

@JvmName(name = "D3ArrayTsoBufferedImage")
fun <T: Number> D3Array<T>.toBufferedImage(): BufferedImage {
    val height = shape[0]
    val width = shape[1]

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val r = this[y, x, 0].between(0, 255).toInt();
            val g = this[y, x, 1].between(0f, 255).toInt();
            val b = this[y, x, 2].between(0f,255).toInt();

            // 组合为 RGB 整数 (alpha 固定为 0xFF)
            val rgb = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

            image.setRGB(x, y, rgb)
        }
    }
    return image
}


@JvmName(name = "D2ArrayTsoBufferedImage")
fun <T: Number> D2Array<T>.toBufferedImage(): BufferedImage {
    val height = shape[0]
    val width = shape[1]

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    val max = (this.max() ?: 1.0).toInt() * 255

    this.forEachMultiIndexed { idxs, value ->
        val y = idxs[0]
        val x = idxs[1]
        val gray = value.toInt() / max
        val rgb = (gray shl 16) or (gray shl 8) or gray
        image.setRGB(x, y, rgb)
    }
    return image
}



@JvmName("D3ArrayIntGamma")
inline fun  D3Array<Int>.gamma(coeff: Float = 2.2f): D3Array<Int> {
    require(coeff > 0) { "Gamma coefficient must be positive" }
    require(this.all { it >= 0 }) { "Input values must be non-negative" }
    val indicator = 1 / coeff
    return this.map { it.toFloat().pow(indicator).toInt() }
}

@JvmName("D3ArrayFloatGamma")
inline fun  D3Array<Float>.gamma(coeff: Float = 2.2f): D3Array<Float> {
    require(coeff > 0) { "Gamma coefficient must be positive" }
    require(this.all { it > 0 }) { "Input values must be non-negative" }
    val indicator = 1 / coeff
    return this.map { it.pow(indicator) }
}


@JvmName("D3ArrayIntGleam")
inline fun D3Array<Int>.gleam(): D3Array<Int> {
    val newArray = gamma(2.2f)

    val height = shape[0]
    val width = shape[1]
    val channels = shape[2]
    for (y in 0 until height) {
        for (x in 0 until width) {
            val average = this[y, x].sum() / channels
            for (c in 0 until channels) {
                newArray[y, x, c] = average
            }
        }
    }
    return newArray
}


@JvmName("D3ArrayFloatGleam")
inline fun D3Array<Float>.gleam(): D3Array<Float> {
    val newArray = gamma(2.2f)

    val height = shape[0]
    val width = shape[1]
    val channels = shape[2]
    for (y in 0 until height) {
        for (x in 0 until width) {
            val average = this[y, x].sum() / channels
            for (c in 0 until channels) {
                newArray[y, x, c] = average
            }
        }
    }
    return newArray
}

@JvmName(name = "D2ArrayFloatToIntegral")
fun  D2Array<Float>.toIntegral(): D2Array<Float> {
    val height = shape[0]
    val width = shape[1]

    val integral = mk.zeros<Float>(height + 1, width + 1)

    for (y in 1..height) {
        for (x in 1..width) {
            integral[y, x] =
                this[y - 1, x - 1] + integral[y, x - 1] + integral[y - 1, x] - integral[y - 1, x - 1]
        }
    }

    return integral
}

@JvmName(name = "D2ArrayIntToIntegral")
fun D2Array<Int>.toIntegral(): D2Array<Int> {
    val height = shape[0]
    val width = shape[1]

    val integral = mk.zeros<Int>(height + 1, width + 1)

    for (y in 1..height) {
        for (x in 1..width) {
            integral[y, x] =
                this[y - 1, x - 1] + integral[y, x - 1] + integral[y - 1, x] - integral[y - 1, x - 1]
        }
    }

    return integral
}


@JvmName("D2ArrayIntToD2Array")
fun D3Array<Float>.toD2Array(): D2Array<Float> {
    val height = shape[0]
    val width = shape[1]

    val intArray = mk.zeros<Float>(height, width)

    for (y in 1 until height) {
        for (x in 1 until width) {
            intArray[y, x] = this[y, x, 0]
        }
    }
    return intArray;
}



@JvmName("D2ArrayIntToD2ArrayInt")
fun D3Array<Int>.toD2Array(): D2Array<Int> {
    val height = shape[0]
    val width = shape[1]

    val intArray = mk.zeros<Int>(height, width)

    for (y in 1 until height) {
        for (x in 1 until width) {
            intArray[y, x] = this[y, x, 0]
        }
    }
    return intArray;
}

@JvmName("D2ArrayFloatMean")
fun List<D2Array<Float>>.mean(): Float {
    return DefaultStatistics.mean(mk.stack(this)).toFloat()
}

@JvmName("D2ArrayIntMean")
fun List<D2Array<Int>>.mean(): Float {
    return DefaultStatistics.mean(mk.stack(this)).toFloat()
}

@JvmName("D2ArrayFloatMean")

fun  List<D2Array<Float>>.std(ddof: Int = 0): Float {
    val m = this.mean()
    var sum = 0.0
    this.forEach { i ->
        i.forEach { j ->
            val diff = j - m
            sum += diff * diff
        }
    }
    return sqrt(sum / (this.size - ddof)).toFloat()
}
@JvmName("D2ArrayIntStd")
fun  List<D2Array<Int>>.std(ddof: Int = 0): Float {
    val m = this.mean()
    var sum = 0.0
    this.forEach { i ->
        i.forEach { j ->
            val diff = j - m
            sum += diff * diff
        }
    }
    return sqrt(sum / (this.size - ddof)).toFloat()
}
