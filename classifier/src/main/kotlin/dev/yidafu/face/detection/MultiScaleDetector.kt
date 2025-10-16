package dev.yidafu.face.detection

import dev.yidafu.face.detection.generated.TrainedCascade
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

/**
 * 多尺度人脸检测器
 */
object MultiScaleDetector {
    
    private const val WINDOW_SIZE = 24  // 训练时的窗口大小
    
    /**
     * 使用级联分类器检测（动态加载）
     */
    private fun detectWithCascade(window: D2Array<Float>): Boolean {
        return try {
            TrainedCascade.detectFace(window)
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(
                "Cascade classifier not found. Please run the trainer module first.",
                e
            )
        }
    }
    
    /**
     * 多尺度人脸检测
     * @param image 输入图像
     * @param scaleFactor 金字塔缩放因子 (通常 1.1-1.2)
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
        
        // 转换为灰度图
        val grayImage = ImageUtils.toGrayscale(image)
        val detections = mutableListOf<FaceDetection>()
        
        // 创建图像金字塔并检测
        var scale = 1.0f
        var scaledImage = grayImage
        var scaleLevel = 0
        
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
            
            if (maxSize > 0 && currentSize > maxSize) {
                break
            }
            
            println("  Scale ${scaleLevel}: ${scaledImage.width} × ${scaledImage.height}, detecting ${currentSize}px faces...")
            
            // 在当前尺度检测
            val scaleDetections = detectAtScale(scaledImage, scale, stepSize)
            detections.addAll(scaleDetections)
            
            println("    Found ${scaleDetections.size} candidates")
            
            // 缩放到下一个尺度
            scale *= scaleFactor
            val newWidth = (grayImage.width / scale).toInt()
            val newHeight = (grayImage.height / scale).toInt()
            
            if (newWidth < WINDOW_SIZE || newHeight < WINDOW_SIZE) break
            
            scaledImage = ImageUtils.resize(grayImage, newWidth, newHeight)
            scaleLevel++
        }
        
        println("Total candidates before NMS: ${detections.size}")
        
        // 非极大值抑制
        val finalDetections = nonMaximumSuppression(detections, 0.3f)
        
        println("Final detections after NMS: ${finalDetections.size}")
        
        return finalDetections
    }
    
    /**
     * 在单个尺度上检测
     */
    private fun detectAtScale(
        scaledImage: BufferedImage,
        scale: Float,
        stepSize: Int
    ): List<FaceDetection> {
        val detections = mutableListOf<FaceDetection>()
        val imageArray = ImageUtils.toArray(scaledImage)
        val integralImage = ImageUtils.toIntegral(imageArray)
        
        val maxY = scaledImage.height - WINDOW_SIZE
        val maxX = scaledImage.width - WINDOW_SIZE
        
        var windowsChecked = 0
        var facesFound = 0
        
        for (y in 0..maxY step stepSize) {
            for (x in 0..maxX step stepSize) {
                windowsChecked++
                
                // 提取窗口
                val window = ImageUtils.extractWindow(integralImage, x, y, WINDOW_SIZE)
                
                // 使用级联分类器检测
                try {
                    if (detectWithCascade(window)) {
                        // 转换回原始图像坐标
                        val detection = FaceDetection(
                            x = (x * scale).toInt(),
                            y = (y * scale).toInt(),
                            width = (WINDOW_SIZE * scale).toInt(),
                            height = (WINDOW_SIZE * scale).toInt(),
                            confidence = 1.0f
                        )
                        detections.add(detection)
                        facesFound++
                    }
                } catch (e: UnsupportedOperationException) {
                    // 分类器未训练
                    if (windowsChecked == 1) {
                        println("    WARNING: Cascade classifier not trained yet!")
                    }
                }
            }
        }
        
        return detections
    }
    
    /**
     * 非极大值抑制（NMS）
     * 合并重叠的检测框
     */
    fun nonMaximumSuppression(
        detections: List<FaceDetection>,
        overlapThreshold: Float = 0.3f
    ): List<FaceDetection> {
        if (detections.isEmpty()) return emptyList()
        
        val result = mutableListOf<FaceDetection>()
        val sorted = detections.sortedByDescending { it.confidence }
        val suppressed = BooleanArray(sorted.size)
        
        for (i in sorted.indices) {
            if (suppressed[i]) continue
            
            result.add(sorted[i])
            
            // 抑制与当前框重叠度高的其他框
            for (j in i + 1 until sorted.size) {
                if (suppressed[j]) continue
                
                val iou = calculateIoU(sorted[i], sorted[j])
                if (iou > overlapThreshold) {
                    suppressed[j] = true
                }
            }
        }
        
        return result
    }
    
    /**
     * 计算两个框的交并比（IoU）
     */
    private fun calculateIoU(box1: FaceDetection, box2: FaceDetection): Float {
        val x1 = max(box1.x, box2.x)
        val y1 = max(box1.y, box2.y)
        val x2 = min(box1.x + box1.width, box2.x + box2.width)
        val y2 = min(box1.y + box1.height, box2.y + box2.height)
        
        val intersection = max(0, x2 - x1) * max(0, y2 - y1)
        val area1 = box1.width * box1.height
        val area2 = box2.width * box2.height
        val union = area1 + area2 - intersection
        
        return if (union > 0) intersection.toFloat() / union else 0f
    }
}
