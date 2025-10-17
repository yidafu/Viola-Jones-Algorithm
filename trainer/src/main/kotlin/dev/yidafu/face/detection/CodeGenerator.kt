package dev.yidafu.face.detection

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

/**
 * 级联分类器代码生成器
 * 使用 KotlinPoet 生成类型安全的 Kotlin 代码
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
        // 生成各个阶段的属性
        val stageProperties = cascade.stages.mapIndexed { index, stage ->
            generateStageProperty(index + 1, stage)
        }
        
        // 生成级联分类器属性
        val cascadeProperty = generateCascadeProperty(cascade)
        
        // 生成检测函数
        val detectFunction = generateDetectFunction()
        
        // 构建完整的 FileSpec
        val fileSpec = FileSpec.builder(packageName, "TrainedCascade")
            .addFileComment("Auto-generated file. Do not edit manually!")
            .addImport("dev.yidafu.face.detection", 
                "CascadeClassifier", "StageClassifier", "WeakClassifier",
                "Feature2h", "Feature2v", "Feature3h", "Feature3v", "Feature4")
            .addType(
                TypeSpec.objectBuilder("TrainedCascade")
                    .addKdoc("""
                        训练好的级联分类器
                        自动生成，包含 ${cascade.stages.size} 个阶段
                        
                        注意：此分类器直接接收归一化后的积分图
                        归一化和积分图计算应该在调用前完成
                    """.trimIndent())
                    .apply {
                        addProperty(PropertySpec.builder("stageCount", INT)
                            .initializer(CodeBlock.of("${cascade.stages.size}"))
                            .build())
                        addProperty(PropertySpec.builder("SAMPLE_MEAN", FLOAT)
                            .initializer(CodeBlock.of("${normMean}f"))
                            .build())
                        addProperty(PropertySpec.builder("SAMPLE_STD", FLOAT)
                            .initializer(CodeBlock.of("${normStd}f"))
                            .build())
                        stageProperties.forEach { addProperty(it) }
                        addProperty(cascadeProperty)
                        addFunction(detectFunction)
                    }
                    .build()
            )
            .build()
        
        // 写入文件
        fileSpec.writeTo(outputDir)
        
        val packagePath = packageName.replace('.', '/')
        val targetFile = outputDir.resolve(packagePath).resolve("TrainedCascade.kt")
        println("Generated code saved to: ${targetFile.absolutePath}")
    }
    
    /**
     * 生成单个阶段的属性
     */
    private fun generateStageProperty(stageNum: Int, stage: StageClassifier): PropertySpec {
        val weakClassifierListType = LIST.parameterizedBy(
            ClassName("dev.yidafu.face.detection", "WeakClassifier")
        )
        
        val initializerCode = CodeBlock.builder()
            .add("listOf(\n")
            .indent()
        
        stage.weakClassifiers.forEachIndexed { index, classifier ->
            val featureCode = generateFeatureCode(classifier.classifier)
            initializerCode.add(
                """WeakClassifier(
                |    threshold = ${classifier.threshold}f,
                |    polarity = ${classifier.polarity},
                |    alpha = ${classifier.alpha}f,
                |    classifier = $featureCode
                |)""".trimMargin()
            )
            if (index < stage.weakClassifiers.size - 1) {
                initializerCode.add(",\n")
            }
        }
        
        initializerCode
            .unindent()
            .add("\n)")
        
        return PropertySpec.builder("stage$stageNum", weakClassifierListType)
            .addModifiers(KModifier.PRIVATE)
            .initializer(initializerCode.build())
            .build()
    }
    
    /**
     * 生成特征构造代码
     */
    private fun generateFeatureCode(feature: Feature): String {
        val className = feature::class.simpleName ?: "Feature"
        return "$className(x=${feature.x}, y=${feature.y}, width=${feature.width}, height=${feature.height})"
    }
    
    /**
     * 生成级联分类器属性
     */
    private fun generateCascadeProperty(cascade: CascadeClassifier): PropertySpec {
        val initializerCode = CodeBlock.builder()
            .add("CascadeClassifier(\n")
            .indent()
            .add("stages = listOf(\n")
            .indent()
        
        cascade.stages.forEachIndexed { index, stage ->
            initializerCode.add(
                "StageClassifier(stage${index + 1}, ${stage.threshold}f)"
            )
            if (index < cascade.stages.size - 1) {
                initializerCode.add(",\n")
            }
        }
        
        initializerCode
            .unindent()
            .add("\n)")
            .unindent()
            .add("\n)")
        
        return PropertySpec.builder(
            "cascade",
            ClassName("dev.yidafu.face.detection", "CascadeClassifier")
        )
            .addModifiers(KModifier.PRIVATE)
            .initializer(initializerCode.build())
            .build()
    }
    
    /**
     * 生成检测函数
     */
    private fun generateDetectFunction(): FunSpec {
        val d2ArrayType = ClassName("org.jetbrains.kotlinx.multik.ndarray.data", "D2Array")
            .parameterizedBy(FLOAT)
        
        return FunSpec.builder("detectFace")
            .addKdoc("""
                检测积分图中是否包含人脸
                @param integralImage 归一化后的积分图
                @return true 表示包含人脸，false 表示不包含
                
                注意：输入应该是已经归一化并计算好的积分图
                使用 ImageUtils.toIntegral() 和归一化处理
            """.trimIndent())
            .addParameter("integralImage", d2ArrayType)
            .returns(BOOLEAN)
            .addStatement("return cascade.predict(integralImage)")
            .build()
    }
}
