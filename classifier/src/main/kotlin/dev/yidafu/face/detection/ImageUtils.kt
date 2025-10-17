package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

/**
 * 图像处理工具类
 */
object ImageUtils {
    
    /**
     * 加载图像
     */
    fun loadImage(file: File): BufferedImage {
        return ImageIO.read(file)
    }
    
    /**
     * 保存图像
     */
    fun saveImage(image: BufferedImage, file: File, format: String = "jpg") {
        ImageIO.write(image, format, file)
    }

    /**
     * BufferedImage 转换为 D2Array<Float>
     */
    fun toArray(image: BufferedImage): D2Array<Float> {
        val height = image.height
        val width = image.width
        val data = FloatArray(height * width)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)
                // 提取灰度值 (0-255)
                val gray = (rgb and 0xFF).toFloat()
                data[y * width + x] = gray
            }
        }
        
        return mk.ndarray(data, height, width)
    }
    
    /**
     * 归一化图像数组
     * @param image 原始图像数组
     * @param mean 训练数据的均值
     * @param std 训练数据的标准差
     * @return 归一化后的图像数组
     */
    fun normalize(
        image: D2Array<Float>,
        mean: Float,
        std: Float
    ): D2Array<Float> {
        val height = image.shape[0]
        val width = image.shape[1]
        val normalized = FloatArray(height * width)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                normalized[idx] = (image[y, x] - mean) / std
            }
        }
        
        return mk.ndarray(normalized, height, width)
    }
    
    /**
     * 缩放图像
     */
    fun resize(image: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage {
        val resized = BufferedImage(newWidth, newHeight, image.type)
        val g = resized.createGraphics()
        g.drawImage(image.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null)
        g.dispose()
        return resized
    }
    
    /**
     * 提取窗口区域
     */
    fun extractWindow(
        integralImage: D2Array<Float>,
        x: Int,
        y: Int,
        windowSize: Int
    ): D2Array<Float> {
        val size = windowSize + 1
        val window = mk.ndarray(FloatArray(size * size), size, size)
        
        for (wy in 0..windowSize) {
            for (wx in 0..windowSize) {
                val py = y + wy
                val px = x + wx
                if (py < integralImage.shape[0] && px < integralImage.shape[1]) {
                    window[wy, wx] = integralImage[py, px]
                }
            }
        }
        
        return window
    }
    
    /**
     * 在图像上绘制矩形框
     */
    fun drawRectangles(
        image: BufferedImage,
        detections: List<FaceDetection>,
        color: Color = Color.RED,
        thickness: Int = 3
    ): BufferedImage {
        val result = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val g = result.createGraphics()
        
        // 绘制原图
        g.drawImage(image, 0, 0, null)
        
        // 绘制矩形框
        g.color = color
        g.stroke = java.awt.BasicStroke(thickness.toFloat())
        
        for (detection in detections) {
            g.drawRect(detection.x, detection.y, detection.width, detection.height)
            
            // 可选：绘制置信度
            if (detection.confidence > 0) {
                g.drawString(
                    String.format("%.2f", detection.confidence),
                    detection.x,
                    max(detection.y - 5, 15)
                )
            }
        }
        
        g.dispose()
        return result
    }
    
    /**
     * 计算积分图
     */
    fun toIntegral(image: D2Array<Float>): D2Array<Float> {
        val height = image.shape[0]
        val width = image.shape[1]
        val integralHeight = height + 1
        val integralWidth = width + 1
        val integral = mk.ndarray(FloatArray(integralHeight * integralWidth), integralHeight, integralWidth)
        
        for (y in 1..height) {
            for (x in 1..width) {
                integral[y, x] = image[y - 1, x - 1] +
                    integral[y - 1, x] +
                    integral[y, x - 1] -
                    integral[y - 1, x - 1]
            }
        }
        
        return integral
    }
}

/**
 * 人脸检测结果
 */
data class FaceDetection(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val confidence: Float = 1.0f
)

