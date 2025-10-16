package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import kotlin.math.abs
import kotlin.math.sign

/**
 * 弱分类器
 */
data class WeakClassifier(
    val threshold: Float,
    val polarity: Int,
    val alpha: Float,
    val classifier: Feature
)

/**
 * 单个级联阶段的分类器
 * @param weakClassifiers 该阶段的弱分类器列表
 * @param threshold 该阶段的决策阈值
 */
data class StageClassifier(
    val weakClassifiers: List<WeakClassifier>,
    val threshold: Float
)

/**
 * Viola-Jones 级联分类器
 * @param stages 各个阶段的分类器列表
 */
data class CascadeClassifier(
    val stages: List<StageClassifier>
) {
    /**
     * 预测给定样本是否为人脸
     * @param x 输入图像的积分图
     * @return true 表示是人脸，false 表示不是人脸
     */
    fun predict(x: D2Array<Float>): Boolean {
        // 逐层判断，任何一层失败就拒绝
        for (stage in stages) {
            if (!evaluateStage(x, stage)) {
                return false
            }
        }
        // 通过所有阶段
        return true
    }
    
    /**
     * 评估单个阶段的分类结果
     * @param x 输入图像的积分图
     * @param stage 该阶段的分类器
     * @return true 表示通过该阶段，false 表示被拒绝
     */
    private fun evaluateStage(x: D2Array<Float>, stage: StageClassifier): Boolean {
        // 计算该阶段所有弱分类器的加权投票得分
        val score = stage.weakClassifiers.sumOf { classifier ->
            val h = weakClassify(x, classifier)
            // 如果分类为正类（人脸），加上该分类器的权重
            if (h == 1) classifier.alpha.toDouble() else 0.0
        }.toFloat()
        
        // 如果得分超过阈值，则通过该阶段
        return score >= stage.threshold
    }
}

/**
 * 弱分类器分类函数
 */
fun weakClassify(x: D2Array<Float>, classifier: WeakClassifier): Int {
    val featureValue = classifier.classifier.compute(x)
    return (classifier.polarity * (classifier.threshold - featureValue).sign.toInt() + 1) / 2
}


