package dev.yidafu.face.detection

import dev.yidafu.face.detection.generated.TrainedCascade
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

/**
 * 人脸检测器 - 对外提供的公共 API
 */
object FaceDetector {
    
    /**
     * 检测图像中是否包含人脸
     * @param integralImage 输入图像的积分图
     * @return true 表示包含人脸，false 表示不包含
     */
    fun detect(integralImage: D2Array<Float>): Boolean {
        // 动态加载训练好的分类器
        return try {
            TrainedCascade.detectFace(integralImage)
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(
                "Cascade classifier not found. Please run the trainer module first to generate the classifier code.",
                e
            )
        }
    }
    
    /**
     * 批量检测多个图像
     * @param integralImages 积分图列表
     * @return 检测结果列表，true 表示包含人脸
     */
    fun detectBatch(integralImages: List<D2Array<Float>>): List<Boolean> {
        return integralImages.map { detect(it) }
    }
    
    /**
     * 获取级联分类器信息
     */
    fun getCascadeInfo(): String {
        return "Viola-Jones Cascade Face Detector"
    }
}


