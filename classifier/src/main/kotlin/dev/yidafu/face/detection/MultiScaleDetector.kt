package dev.yidafu.face.detection

import java.awt.image.BufferedImage

/**
 * 多尺度人脸检测器 - 多尺度层
 * 基于 SimpleDetector 实现多尺度检测
 */
object MultiScaleDetector {
    
    private const val WINDOW_SIZE = 24  // 训练时的窗口大小
    
    /**
     * 多尺度人脸检测
     * @param image 输入图像
     * @param scaleFactor 金字塔缩放因子 (通常 1.05-1.2)
     * @param stepSize 滑动窗口步长 (像素)
     * @param minSize 最小检测尺寸
     * @param maxSize 最大检测尺寸 (0 表示无限制)
     * @return 检测到的人脸列表
     */
    fun detectMultiScale(
        image: BufferedImage,
        scaleFactor: Float = 1.2f,
        stepSize: Int = 2,
        minSize: Int = 24,
        maxSize: Int = 0
    ): List<FaceDetection> {
        println("\n=== Multi-Scale Face Detection ===")
        println("Image size: ${image.width} × ${image.height}")
        println("Scale factor: $scaleFactor, Step size: $stepSize")

        // 预处理：gleam 转灰度
        val grayImage = image.toD3Array().gleam().toBufferedImage()
        val allDetections = mutableListOf<FaceDetection>()
        
        // 创建图像金字塔并检测
        var scale = 1.0f
        var scaledImage = grayImage
        var scaleLevel = 1
        
        while (scaledImage.width >= WINDOW_SIZE && scaledImage.height >= WINDOW_SIZE) {
            val currentSize = (WINDOW_SIZE * scale).toInt()
            
            // 检查尺寸限制
            if (currentSize < minSize) {
                scale *= scaleFactor
                scaledImage = ImageUtils.resize(
                    grayImage,
                    (grayImage.width / scale).toInt(),
                    (grayImage.height / scale).toInt()
                )
                scaleLevel++
                continue
            }
            
            if (maxSize in 1..<currentSize) {
                break
            }

            println("  Scale ${scaleLevel}: ${scaledImage.width} × ${scaledImage.height}, detecting ${currentSize}px faces...")
            
            // 使用 SimpleDetector 在当前尺度检测
            val scaleDetections = SimpleDetector.detect(scaledImage, stepSize)
            
            println("    Found ${scaleDetections.size} candidates at this scale")
            
            // 转换坐标到原始图像尺度
            val transformedDetections = scaleDetections.map { detection ->
                detection.copy(
                    x = (detection.x * scale).toInt(),
                    y = (detection.y * scale).toInt(),
                    width = (detection.width * scale).toInt(),
                    height = (detection.height * scale).toInt()
                )
            }
            allDetections.addAll(transformedDetections)
            
            // 缩放到下一个尺度
            scale *= scaleFactor
            val newWidth = (grayImage.width / scale).toInt()
            val newHeight = (grayImage.height / scale).toInt()
            
            if (newWidth < WINDOW_SIZE || newHeight < WINDOW_SIZE) break
            scaledImage = ImageUtils.resize(grayImage, newWidth, newHeight)
            scaleLevel++
        }
        
        println("Total candidates before NMS: ${allDetections.size}")
        
        // 非极大值抑制 - 合并所有尺度的检测结果
        val finalDetections = SimpleDetector.nonMaximumSuppression(allDetections, 0.3f)
        
        println("Final detections after NMS: ${finalDetections.size}")
        
        return finalDetections
    }
    
}
