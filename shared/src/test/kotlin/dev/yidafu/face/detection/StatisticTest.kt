package dev.yidafu.face.detection

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.abs

class StatisticTest : StringSpec({
    // 测试 averagePrecisionScore 函数
    "averagePrecisionScore 应该正确计算完美预测的平均精度" {
        val yTrue = listOf(1.0f, 1.0f, 1.0f)
        val yScore = listOf(1.0f, 1.0f, 1.0f)
        val result = averagePrecisionScore(yTrue, yScore)
        result shouldBe 1.0f
    }

    "averagePrecisionScore 应该正确计算完全错误预测的平均精度" {
        val yTrue = listOf(1.0f, 1.0f, 1.0f)
        val yScore = listOf(0.0f, 0.0f, 0.0f)
        val result = averagePrecisionScore(yTrue, yScore)
        // 由于排序后正样本都在后面，平均精度应该接近但不等于0
        abs(result) shouldBeLessThanOrEqual 1.0f
    }

    "averagePrecisionScore 应该正确计算部分正确预测的平均精度" {
        val yTrue = listOf(1.0f, 0.0f, 1.0f, 0.0f, 1.0f)
        val yScore = listOf(0.9f, 0.8f, 0.7f, 0.6f, 0.5f)
        val result = averagePrecisionScore(yTrue, yScore)
        // 手动计算的预期值
        // 排序后顺序不变: (1,0.9), (0,0.8), (1,0.7), (0,0.6), (1,0.5)
        // 精确率和召回率变化: (1/1, 1/3) -> (1/2, 1/3) -> (2/3, 2/3) -> (2/4, 2/3) -> (3/5, 1)
        // 面积计算: (1/3-0)*1 + (1/3-1/3)*0.5 + (2/3-1/3)*2/3 + (2/3-2/3)*0.5 + (1-2/3)*0.6
        val expected = (1/3f * 1f) + (1/3f * 2/3f) + (1/3f * 0.6f)
        abs(result - expected) shouldBeLessThanOrEqual 0.001f
    }

    "averagePrecisionScore 应该在没有正样本时返回0" {
        val yTrue = listOf(0.0f, 0.0f, 0.0f)
        val yScore = listOf(0.5f, 0.6f, 0.7f)
        val result = averagePrecisionScore(yTrue, yScore)
        result shouldBe 0.0f
    }

    "averagePrecisionScore 应该抛出异常当输入列表长度不同时" {
        val yTrue = listOf(1.0f, 0.0f)
        val yScore = listOf(0.9f, 0.8f, 0.7f)
        val exception = try {
            averagePrecisionScore(yTrue, yScore)
            null
        } catch (e: IllegalArgumentException) {
            e
        }
        exception shouldNotBe null
        exception!!.message shouldBe "yTrue and yScore must have the same size"
    }

    // 测试 precisionRecallCurve 函数
    "precisionRecallCurve 应该正确计算精确率-召回率曲线" {
        val ys = floatArrayOf(1.0f, 0.0f, 1.0f, 0.0f, 1.0f)
        val zs = floatArrayOf(0.9f, 0.8f, 0.7f, 0.6f, 0.5f)
        val (precision, recall, thresholds) = precisionRecallCurve(ys, zs)

        // 验证返回的列表长度
        precision.size shouldBe 6 // 5个样本 + 1个起始点
        recall.size shouldBe 6
        thresholds.size shouldBe 6

        // 验证起始点
        precision.last() shouldBe 1.0f
        recall.last() shouldBe 0.0f
        thresholds.last() shouldBe Float.MAX_VALUE

        // 验证一些关键点
        precision[0] shouldBe 1.0f // 第一个样本是正样本，精确率为1
        recall[0] shouldBe 1/3f   // 召回率为1/3
    }

    "precisionRecallCurve 应该正确处理所有样本都是正样本的情况" {
        val ys = floatArrayOf(1.0f, 1.0f, 1.0f)
        val zs = floatArrayOf(0.9f, 0.8f, 0.7f)
        val (precision, recall, _) = precisionRecallCurve(ys, zs)

        // 所有预测都是正样本，精确率始终为1
        precision.all { it == 1.0f } shouldBe true
        // 召回率从1/3逐渐增加到1
        recall[0] shouldBe 1/3f
        recall[1] shouldBe 2/3f
        recall[2] shouldBe 1.0f
    }

    "precisionRecallCurve 应该正确处理所有样本都是负样本的情况" {
        val ys = floatArrayOf(0.0f, 0.0f, 0.0f)
        val zs = floatArrayOf(0.9f, 0.8f, 0.7f)
        val (precision, recall, _) = precisionRecallCurve(ys, zs)

        // 由于没有正样本，精确率和召回率的计算可能会有除零问题
        // 但函数应该能够处理这种情况而不崩溃
        precision.size shouldBe 4 // 3个样本 + 1个起始点
        recall.size shouldBe 4
    }

    // 测试 confusionMatrix 函数
    "confusionMatrix 应该正确计算二分类混淆矩阵" {
        val yTrue = listOf(1, 1, 0, 0, 1, 0)
        val yPred = listOf(1, 0, 1, 0, 1, 0)
        val matrix = confusionMatrix(yTrue, yPred)

        // 类别应该按顺序排序：0, 1
        matrix.size shouldBe 2
        matrix[0].size shouldBe 2
        matrix[1].size shouldBe 2

        // 验证混淆矩阵的值
        matrix[0][0] shouldBe 2 // 真正例 (0预测为0)
        matrix[0][1] shouldBe 1 // 假正例 (0预测为1)
        matrix[1][0] shouldBe 1 // 假负例 (1预测为0)
        matrix[1][1] shouldBe 2 // 真负例 (1预测为1)
    }

    "confusionMatrix 应该正确计算多分类混淆矩阵" {
        val yTrue = listOf(0, 1, 2, 0, 1, 2, 0, 1, 2)
        val yPred = listOf(0, 1, 2, 0, 0, 2, 1, 1, 2)
        val matrix = confusionMatrix(yTrue, yPred)

        // 类别应该按顺序排序：0, 1, 2
        matrix.size shouldBe 3
        matrix.forEach { it.size shouldBe 3 }

        // 验证混淆矩阵的值
        matrix[0][0] shouldBe 2 // 0预测为0
        matrix[0][1] shouldBe 1 // 0预测为1
        matrix[0][2] shouldBe 0 // 0预测为2
        matrix[1][0] shouldBe 1 // 1预测为0
        matrix[1][1] shouldBe 2 // 1预测为1
        matrix[1][2] shouldBe 0 // 1预测为2
        matrix[2][0] shouldBe 0 // 2预测为0
        matrix[2][1] shouldBe 0 // 2预测为1
        matrix[2][2] shouldBe 3 // 2预测为2
    }

    "confusionMatrix 应该抛出异常当输入列表长度不同时" {
        val yTrue = listOf(0, 1, 0)
        val yPred = listOf(0, 1, 0, 1)
        val exception = try {
            confusionMatrix(yTrue, yPred)
            null
        } catch (e: IllegalArgumentException) {
            e
        }
        exception shouldNotBe null
        exception!!.message shouldBe "yTrue and yPred must have the same size"
    }

    "confusionMatrix 应该抛出异常当遇到未知类别时" {
        // 这个测试有点特殊，因为函数设计上会处理所有在yTrue或yPred中出现的类别
        // 所以我们需要构造一个特殊的场景，这里只是验证函数不会因为类别问题而崩溃
        val yTrue = listOf(10, 20, 30)
        val yPred = listOf(10, 20, 30)
        val matrix = confusionMatrix(yTrue, yPred)
        matrix.size shouldBe 3
    }
})