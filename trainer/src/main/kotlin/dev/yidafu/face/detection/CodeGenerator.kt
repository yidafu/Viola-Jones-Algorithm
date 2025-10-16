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
        packageName: String = "dev.yidafu.face.detection.generated",
        outputDir: File
    ) {
        val code = buildString {
            appendLine("// Auto-generated file. Do not edit manually!")
            appendLine("package $packageName")
            appendLine()
            appendLine("import dev.yidafu.face.detection.*")
            appendLine("import org.jetbrains.kotlinx.multik.ndarray.data.D2Array")
            appendLine()
            appendLine("/**")
            appendLine(" * 训练好的级联分类器")
            appendLine(" * 自动生成，包含 ${cascade.stages.size} 个阶段")
            appendLine(" */")
            appendLine("object TrainedCascade {")
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
            
            // 生成检测函数
            appendLine("    /**")
            appendLine("     * 检测积分图中是否包含人脸")
            appendLine("     * @param integralImage 输入图像的积分图")
            appendLine("     * @return true 表示包含人脸，false 表示不包含")
            appendLine("     */")
            appendLine("    fun detectFace(integralImage: D2Array<Float>): Boolean {")
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
