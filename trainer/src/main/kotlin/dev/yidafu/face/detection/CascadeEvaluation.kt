package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

/**
 * 级联分类器评估结果
 */
data class CascadeEvaluationResult(
    val truePositives: Int,
    val falsePositives: Int,
    val trueNegatives: Int,
    val falseNegatives: Int,
    val stageRejects: List<Int>
) {
    val precision: Float
        get() = if (truePositives + falsePositives > 0) {
            truePositives.toFloat() / (truePositives + falsePositives)
        } else 0f
    
    val recall: Float
        get() = if (truePositives + falseNegatives > 0) {
            truePositives.toFloat() / (truePositives + falseNegatives)
        } else 0f
    
    val accuracy: Float
        get() {
            val total = truePositives + falsePositives + trueNegatives + falseNegatives
            return if (total > 0) {
                (truePositives + trueNegatives).toFloat() / total
            } else 0f
        }
    
    val f1Score: Float
        get() = if (precision + recall > 0) {
            2 * precision * recall / (precision + recall)
        } else 0f
    
    fun printReport() {
        println("\n=== Cascade Classifier Evaluation ===")
        println("Confusion Matrix:")
        println("  True Positives:  $truePositives")
        println("  False Positives: $falsePositives")
        println("  True Negatives:  $trueNegatives")
        println("  False Negatives: $falseNegatives")
        println("\nMetrics:")
        println("  Accuracy:  ${String.format("%.2f%%", accuracy * 100)}")
        println("  Precision: ${String.format("%.2f%%", precision * 100)}")
        println("  Recall:    ${String.format("%.2f%%", recall * 100)}")
        println("  F1-Score:  ${String.format("%.4f", f1Score)}")
        println("\nStage Rejection Statistics:")
        stageRejects.forEachIndexed { index, count ->
            val total = truePositives + falsePositives + trueNegatives + falseNegatives
            val percentage = count.toFloat() / total * 100
            println("  Stage ${index + 1}: $count samples (${String.format("%.2f%%", percentage)})")
        }
    }
}

/**
 * 评估级联分类器在多个样本上的详细性能
 */
fun evaluateCascadeDetailed(
    cascade: CascadeClassifier,
    samples: List<D2Array<Float>>,
    labels: List<Float>
): CascadeEvaluationResult {
    val stageRejects = MutableList(cascade.stages.size) { 0 }
    var tp = 0  // 真阳性
    var fp = 0  // 假阳性
    var tn = 0  // 真阴性
    var fn = 0  // 假阴性
    
    for ((x, y) in samples.zip(labels)) {
        val actual = y >= 0.5f
        var rejected = false
        
        // 逐阶段评估
        for ((index, stage) in cascade.stages.withIndex()) {
            if (!evaluateStage(x, stage)) {
                rejected = true
                stageRejects[index]++
                break
            }
        }
        
        val predicted = !rejected
        
        // 统计混淆矩阵
        when {
            predicted && actual -> tp++
            predicted && !actual -> fp++
            !predicted && actual -> fn++
            !predicted && !actual -> tn++
        }
    }
    
    return CascadeEvaluationResult(
        truePositives = tp,
        falsePositives = fp,
        trueNegatives = tn,
        falseNegatives = fn,
        stageRejects = stageRejects
    )
}

private fun evaluateStage(x: D2Array<Float>, stage: StageClassifier): Boolean {
    val score = stage.weakClassifiers.sumOf { classifier ->
        val h = weakClassify(x, classifier)
        if (h == 1) classifier.alpha.toDouble() else 0.0
    }.toFloat()
    return score >= stage.threshold
}

/**
 * 计算阶段分类器的阈值
 */
fun calculateThreshold(
    classifiers: List<WeakClassifier>,
    targetFalsePositiveRate: Float = 0.5f
): Float {
    val totalAlpha = classifiers.sumOf { it.alpha.toDouble() }.toFloat()
    return totalAlpha * targetFalsePositiveRate
}


