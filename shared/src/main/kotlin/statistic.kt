package dev.yidafu.face.detection


/**
 * 计算平均精度（Average Precision）
 *
 * @param yTrue 真实标签列表（0或1）
 * @param yScore 预测分数列表（概率值）
 * @return 平均精度值（范围[0,1]）
 */
fun averagePrecisionScore(yTrue: List<Float>, yScore: List<Float>): Float {
    require(yTrue.size == yScore.size) { "yTrue and yScore must have the same size" }

    // 1. 将样本按预测分数降序排序
    val sortedPairs = yTrue.zip(yScore).sortedByDescending { it.second }

    // 2. 计算正样本总数
    val totalPositives = yTrue.count { it.toInt() == 1 }.toFloat()
    if (totalPositives == 0.0f) return 0.0f // 如果没有正样本，返回0

    // 3. 初始化变量
    var truePositives = 0.0f
    var falsePositives = 0.0f
    var accumulatedPrecision = 0.0f
    var previousRecall = 0.0f

    // 4. 遍历排序后的样本
    for ((label, _) in sortedPairs) {
        if (label.toInt() == 1) {
            truePositives += 1
        } else {
            falsePositives += 1
        }

        // 计算当前精确率和召回率
        val precision = truePositives / (truePositives + falsePositives)
        val recall = truePositives / totalPositives

        // 5. 计算矩形面积并累加（使用梯形法则）
        accumulatedPrecision += (recall - previousRecall) * precision

        // 更新上一次的召回率
        previousRecall = recall
    }

    return accumulatedPrecision
}

fun precisionRecallCurve(ys: FloatArray, zs: FloatArray): Triple<List<Float>, List<Float>, List<Float>> {
    // 按预测分数降序排序
    val sortedPairs = ys.zip(zs).sortedByDescending { it.second }

    val precision = mutableListOf<Float>()
    val recall = mutableListOf<Float>()
    val thresholds = mutableListOf<Float>()

    var truePositives = 0f
    var falsePositives = 0f
    val totalPositives = ys.count { it > 0.5f }.toFloat()

    for ((label, score) in sortedPairs) {
        if (label.toInt() == 1) truePositives++ else falsePositives++

        val p: Float = truePositives / (truePositives + falsePositives)
        val r = truePositives / totalPositives

        precision.add(p)
        recall.add(r)
        thresholds.add(score)
    }

    // 添加起始点（召回率=0，精确率=1）
    precision.add(1.0f)
    recall.add(0.0f)
    thresholds.add(Float.MAX_VALUE)

    return Triple(precision, recall, thresholds)
}


/**
 * 计算混淆矩阵
 *
 * @param yTrue 真实标签列表
 * @param yPred 预测标签列表
 * @return 混淆矩阵（二维数组）
 */
fun confusionMatrix(yTrue: List<Int>, yPred: List<Int>): Array<Array<Int>> {
    require(yTrue.size == yPred.size) { "yTrue and yPred must have the same size" }

    // 获取所有类别标签
    val classes = (yTrue + yPred).distinct().sorted()
    val classCount = classes.size

    // 初始化混淆矩阵
    val matrix = Array(classCount) { Array(classCount) { 0 } }

    // 创建类别到索引的映射
    val classToIndex = classes.withIndex().associate { (index, value) -> value to index }

    // 填充混淆矩阵
    for (i in yTrue.indices) {
        val trueIndex = classToIndex[yTrue[i]] ?: throw IllegalArgumentException("Unknown class: ${yTrue[i]}")
        val predIndex = classToIndex[yPred[i]] ?: throw IllegalArgumentException("Unknown class: ${yPred[i]}")
        matrix[trueIndex][predIndex]++
    }

    return matrix
}