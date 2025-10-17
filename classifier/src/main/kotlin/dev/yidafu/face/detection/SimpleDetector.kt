package dev.yidafu.face.detection

import dev.yidafu.face.detection.generated.TrainedCascade
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

/**
 * 简单检测器 - 基础层
 * 在单张图像上检测固定24×24窗口的人脸
 */
object SimpleDetector {
    
    private const val WINDOW_SIZE = 24  // 训练时的固定窗口大小
    
    /**
     * 在单张图像上检测固定24×24的人脸
     * @param image 输入图像（将自动进行预处理）
     * @param stepSize 滑动窗口步长（像素）
     * @return 检测到的人脸列表
     */
    fun detect(
        image: BufferedImage,
        stepSize: Int = 2
    ): List<FaceDetection> {
        // 1. 预处理：gleam() + 归一化 + 积分图
        val preprocessed = preprocessImage(image)
        
        // 2. 滑动窗口检测
        val detections = mutableListOf<FaceDetection>()
        val maxY = image.height - WINDOW_SIZE
        val maxX = image.width - WINDOW_SIZE
        
        if (maxY < 0 || maxX < 0) {
            return emptyList()  // 图像太小，无法检测
        }
        
        var windowsChecked = 0
        for (y in 0..maxY step stepSize) {
            for (x in 0..maxX step stepSize) {
                windowsChecked++
                val window = ImageUtils.extractWindow(
                    preprocessed, x, y, WINDOW_SIZE
                )
                
                if (TrainedCascade.detectFace(window)) {
                    detections.add(FaceDetection(
                        x = x,
                        y = y,
                        width = WINDOW_SIZE,
                        height = WINDOW_SIZE,
                        confidence = 1.0f
                    ))
                }
            }
        }
        
        // 3. NMS 合并重叠框
        return nonMaximumSuppression(detections, 0.3f)
    }
    
    /**
     * 预处理图像（与训练时完全一致）
     * 流程: gleam() + normalize() + toIntegral()
     */
    private fun preprocessImage(image: BufferedImage): D2Array<Float> {
        // 使用 shared 模块的扩展函数进行 gleam 处理
        val grayImage = image.toD3Array().gleam().toBufferedImage()
        
        // 转换为数组
        val imageArray = ImageUtils.toArray(grayImage)
        
        // 归一化（使用训练时的参数）
        val normalized = ImageUtils.normalize(
            imageArray,
            TrainedCascade.SAMPLE_MEAN,
            TrainedCascade.SAMPLE_STD
        )
        
        // 计算积分图
        return ImageUtils.toIntegral(normalized)
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

