package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get

/**
 * 基础特征接口 - 用于人脸检测推理
 */
sealed class Feature(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    /**
     * 计算特征值
     * @param integralImage 积分图
     * @return 特征值
     */
    abstract fun compute(integralImage: D2Array<Float>): Float
}

/**
 * 2区域水平特征
 */
class Feature2h(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : Feature(x, y, width, height) {
    private val halfWidth = width / 2
    
    override fun compute(integralImage: D2Array<Float>): Float {
        val left = rectangleSum(integralImage, x, y, halfWidth, height)
        val right = rectangleSum(integralImage, x + halfWidth, y, halfWidth, height)
        return left - right
    }
}

/**
 * 2区域垂直特征
 */
class Feature2v(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : Feature(x, y, width, height) {
    private val halfHeight = height / 2
    
    override fun compute(integralImage: D2Array<Float>): Float {
        val top = rectangleSum(integralImage, x, y, width, halfHeight)
        val bottom = rectangleSum(integralImage, x, y + halfHeight, width, halfHeight)
        return -top + bottom
    }
}

/**
 * 3区域水平特征
 */
class Feature3h(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : Feature(x, y, width, height) {
    private val thirdWidth = width / 3
    
    override fun compute(integralImage: D2Array<Float>): Float {
        val left = rectangleSum(integralImage, x, y, thirdWidth, height)
        val middle = rectangleSum(integralImage, x + thirdWidth, y, thirdWidth, height)
        val right = rectangleSum(integralImage, x + 2 * thirdWidth, y, thirdWidth, height)
        return -left + middle - right
    }
}

/**
 * 3区域垂直特征
 */
class Feature3v(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : Feature(x, y, width, height) {
    private val thirdHeight = height / 3
    
    override fun compute(integralImage: D2Array<Float>): Float {
        val top = rectangleSum(integralImage, x, y, width, thirdHeight)
        val middle = rectangleSum(integralImage, x, y + thirdHeight, width, thirdHeight)
        val bottom = rectangleSum(integralImage, x, y + 2 * thirdHeight, width, thirdHeight)
        return -top + middle - bottom
    }
}

/**
 * 4区域对角特征
 */
class Feature4(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : Feature(x, y, width, height) {
    private val halfWidth = width / 2
    private val halfHeight = height / 2
    
    override fun compute(integralImage: D2Array<Float>): Float {
        val topLeft = rectangleSum(integralImage, x, y, halfWidth, halfHeight)
        val topRight = rectangleSum(integralImage, x + halfWidth, y, halfWidth, halfHeight)
        val bottomLeft = rectangleSum(integralImage, x, y + halfHeight, halfWidth, halfHeight)
        val bottomRight = rectangleSum(integralImage, x + halfWidth, y + halfHeight, halfWidth, halfHeight)
        return topLeft - topRight - bottomLeft + bottomRight
    }
}

/**
 * 使用积分图快速计算矩形区域和
 */
private fun rectangleSum(
    integralImage: D2Array<Float>,
    x: Int,
    y: Int,
    width: Int,
    height: Int
): Float {
    val x1 = x
    val y1 = y
    val x2 = x + width
    val y2 = y + height
    
    val a = if (x1 > 0 && y1 > 0) integralImage[y1 - 1, x1 - 1] else 0f
    val b = if (y1 > 0) integralImage[y1 - 1, x2] else 0f
    val c = if (x1 > 0) integralImage[y2, x1 - 1] else 0f
    val d = integralImage[y2, x2]
    
    return d - b - c + a
}


