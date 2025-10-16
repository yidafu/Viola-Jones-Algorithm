package dev.yidafu.face.detection

import java.io.File

/**
 * 级联分类器代码生成器
 * 将训练好的级联分类器转换为 Kotlin 代码
 */
object ClassifierCodeGenerator {
    
    /**
     * 生成级联分类器代码
     */
    fun generateCascadeClassifier(
        cascade: CascadeClassifier,
        normMean: Float,
        normStd: Float,
        packageName: String = "dev.yidafu.face.detection.generated",
        outputDir: File
    ) {
        val code = buildString {
            appendLine("// Auto-generated file. Do not edit manually!")
            appendLine("package $packageName")
            appendLine()
            appendLine("import dev.yidafu.face.detection.*")
            appendLine("import org.jetbrains.kotlinx.multik.ndarray.data.D2Array")
            appendLine("import org.jetbrains.kotlinx.multik.api.mk")
            appendLine("import org.jetbrains.kotlinx.multik.api.ndarray")
            appendLine("import org.jetbrains.kotlinx.multik.ndarray.data.get")
            appendLine("import org.jetbrains.kotlinx.multik.ndarray.data.set")
            appendLine()
            appendLine("/**")
            appendLine(" * 训练好的级联分类器")
            appendLine(" * 自动生成，包含 ${cascade.stages.size} 个阶段")
            appendLine(" */")
            appendLine("object TrainedCascade {")
            appendLine()
            appendLine("    // 归一化参数（训练时的统计值）")
            appendLine("    private const val NORM_MEAN = ${normMean}f")
            appendLine("    private const val NORM_STD = ${normStd}f")
            appendLine()
            
            // 生成每个阶段
            cascade.stages.forEachIndexed { index, stage ->
                appendLine("    private val stage${index + 1}: List<WeakClassifier> = listOf(")
                stage.weakClassifiers.forEachIndexed { wIndex, classifier ->
                    val featureCode = generateFeatureCode(classifier.classifier)
                    appendLine("        WeakClassifier(")
                    appendLine("            threshold = ${classifier.threshold}f,")
                    appendLine("            polarity = ${classifier.polarity},")
                    appendLine("            alpha = ${classifier.alpha}f,")
                    appendLine("            classifier = $featureCode")
                    append("        )")
                    if (wIndex < stage.weakClassifiers.size - 1) {
                        appendLine(",")
                    } else {
                        appendLine()
                    }
                }
                appendLine("    )")
                appendLine()
            }
            
            // 生成级联分类器
            appendLine("    private val cascade = CascadeClassifier(")
            appendLine("        stages = listOf(")
            cascade.stages.forEachIndexed { index, stage ->
                append("            StageClassifier(stage${index + 1}, ${stage.threshold}f)")
                if (index < cascade.stages.size - 1) {
                    appendLine(",")
                } else {
                    appendLine()
                }
            }
            appendLine("        )")
            appendLine("    )")
            appendLine()
            
            // 生成归一化函数
            appendLine("    /**")
            appendLine("     * 归一化图像数组")
            appendLine("     */")
            appendLine("    private fun normalizeImage(image: D2Array<Float>): D2Array<Float> {")
            appendLine("        val height = image.shape[0]")
            appendLine("        val width = image.shape[1]")
            appendLine("        val normalized: D2Array<Float> = mk.ndarray(FloatArray(height * width), height, width)")
            appendLine("        ")
            appendLine("        for (y in 0 until height) {")
            appendLine("            for (x in 0 until width) {")
            appendLine("                normalized[y, x] = (image[y, x] - NORM_MEAN) / NORM_STD")
            appendLine("            }")
            appendLine("        }")
            appendLine("        ")
            appendLine("        return normalized")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 计算积分图")
            appendLine("     */")
            appendLine("    private fun toIntegral(image: D2Array<Float>): D2Array<Float> {")
            appendLine("        val height = image.shape[0]")
            appendLine("        val width = image.shape[1]")
            appendLine("        val integralHeight = height + 1")
            appendLine("        val integralWidth = width + 1")
            appendLine("        val integral: D2Array<Float> = mk.ndarray(FloatArray(integralHeight * integralWidth), integralHeight, integralWidth)")
            appendLine("        ")
            appendLine("        for (y in 1..height) {")
            appendLine("            for (x in 1..width) {")
            appendLine("                integral[y, x] = image[y - 1, x - 1] +")
            appendLine("                    integral[y - 1, x] +")
            appendLine("                    integral[y, x - 1] -")
            appendLine("                    integral[y - 1, x - 1]")
            appendLine("            }")
            appendLine("        }")
            appendLine("        ")
            appendLine("        return integral")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 检测原始图像中是否包含人脸")
            appendLine("     * @param image 原始图像数组 (未归一化，像素值 0-255)")
            appendLine("     * @return true 表示包含人脸，false 表示不包含")
            appendLine("     */")
            appendLine("    fun detectFace(image: D2Array<Float>): Boolean {")
            appendLine("        val normalized = normalizeImage(image)")
            appendLine("        val integralImage = toIntegral(normalized)")
            appendLine("        return cascade.predict(integralImage)")
            appendLine("    }")
            appendLine("}")
        }
        
        // 写入文件
        val packagePath = packageName.replace('.', '/')
        val targetDir = outputDir.resolve(packagePath)
        targetDir.mkdirs()
        
        val targetFile = targetDir.resolve("TrainedCascade.kt")
        targetFile.writeText(code)
        
        println("Generated code saved to: ${targetFile.absolutePath}")
    }
    
    /**
     * 生成特征构造代码
     */
    private fun generateFeatureCode(feature: Feature): String {
        val className = feature::class.simpleName ?: "Feature"
        return "$className(x=${feature.x}, y=${feature.y}, width=${feature.width}, height=${feature.height})"
    }
}
