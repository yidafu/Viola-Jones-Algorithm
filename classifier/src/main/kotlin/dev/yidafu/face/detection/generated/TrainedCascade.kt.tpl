package dev.yidafu.face.detection.generated

import dev.yidafu.face.detection.*
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

/**
 * 占位符 - 将在训练后自动生成
 * 训练好的级联分类器
 */
object TrainedCascade {
    
    /**
     * 检测积分图中是否包含人脸
     * @param integralImage 归一化后的积分图
     * @return true 表示包含人脸，false 表示不包含
     */
    fun detectFace(integralImage: D2Array<Float>): Boolean {
        throw UnsupportedOperationException(
            "Cascade classifier has not been trained yet. " +
            "Please run the trainer module first to generate this code."
        )
    }
}
