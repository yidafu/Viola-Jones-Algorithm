package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.nio.file.Paths


suspend fun main() {
    // 加载图像文件路径
    val backgroundImages = loadImage(Paths.get(BACKGROUND_IMAGE_DIR))
    val faceImages = loadImage(Paths.get(FACE_IMAGE_DIR))
    
    println("Loaded ${faceImages.size} face images")
    println("Loaded ${backgroundImages.size} background images")

    // 使用足够的样本以确保训练质量
    val numPositive = minOf(1500, faceImages.size)  // 增加到1500
    val numNegative = minOf(1500, backgroundImages.size)
    println("Using $numPositive positive samples and $numNegative negative samples")
    
    val (xs, ys, normParams) = sampleDataNormalized(numPositive, numNegative, faceImages, backgroundImages)
    val xis = xs.map { it.toIntegral() }
    
    println("Normalization parameters: mean=${normParams.mean}, std=${normParams.std}")

    // 创建特征
    val features = createAllFeatures(WINDOW_SIZE)
    
    println("\n" + "=".repeat(80))
    println("=== Viola-Jones Cascade Classifier Training ===")
    println("=".repeat(80))
    
    // ===== 第一阶段：3个弱分类器 =====
    println("\n=== Stage 1: Training 3 weak classifiers ===")
    val (stage1Classifiers, stage1Weights) = buildWeakClassifiers(
        "stage1", 3, xis, ys.toList(), features
    )
    @Suppress("UNCHECKED_CAST")
    val stage1List = stage1Classifiers as List<WeakClassifier>
    val stage1Threshold = calculateThreshold(stage1List, 0.45f)  // 45% - 提高召回率
    println("Stage 1 threshold: ${String.format("%.4f", stage1Threshold)}")
    
    // ===== 第二阶段：15个弱分类器 ====w
    println("\n=== Stage 2: Training 15 weak classifiers ===")
    val (stage2Classifiers, stage2Weights) = buildWeakClassifiers(
        "stage2", 15, xis, ys.toList(), features,
        ws = stage1Weights.last()
    )
    @Suppress("UNCHECKED_CAST")
    val stage2List = stage2Classifiers as List<WeakClassifier>
    val stage2Threshold = calculateThreshold(stage2List, 0.55f)  // 55% - 平衡召回率和精确率
    println("Stage 2 threshold: ${String.format("%.4f", stage2Threshold)}")
    
    // ===== 第三阶段：30个弱分类器 =====
    println("\n=== Stage 3: Training 30 weak classifiers ===")
    val (stage3Classifiers, stage3Weights) = buildWeakClassifiers(
        "stage3", 30, xis, ys.toList(), features,
        ws = stage2Weights.last()
    )
    @Suppress("UNCHECKED_CAST")
    val stage3List = stage3Classifiers as List<WeakClassifier>
    val stage3Threshold = calculateThreshold(stage3List, 0.7f)  // 70% - 最严格，精确筛选
    println("Stage 3 threshold: ${String.format("%.4f", stage3Threshold)}")
    
    // ===== 构建级联分类器 =====
    println("\n" + "=".repeat(80))
    println("=== Building Cascade Classifier ===")
    val cascade = CascadeClassifier(listOf(
        StageClassifier(stage1List, stage1Threshold),
        StageClassifier(stage2List, stage2Threshold),
        StageClassifier(stage3List, stage3Threshold)
    ))
    println("Cascade has ${cascade.stages.size} stages:")
    cascade.stages.forEachIndexed { index, stage ->
        println("  Stage ${index + 1}: ${stage.weakClassifiers.size} weak classifiers, threshold = ${String.format("%.4f", stage.threshold)}")
    }
    
    // ===== 评估级联分类器 =====
    println("\n" + "=".repeat(80))
    println("=== Evaluating Cascade Classifier ===")
    val result = evaluateCascadeDetailed(cascade, xis, ys.toList())
    result.printReport()
    
    // ===== 生成代码到 classifier 模块 =====
    println("\n" + "=".repeat(80))
    println("=== Generating Classifier Code ===")
    // 获取项目根目录（向上查找直到找到 settings.gradle.kts）
    var projectRoot = java.io.File("").absoluteFile
    while (projectRoot != null && !projectRoot.resolve("settings.gradle.kts").exists()) {
        projectRoot = projectRoot.parentFile
    }
    if (projectRoot == null) {
        projectRoot = java.io.File("").absoluteFile
    }
    val classifierOutputDir = projectRoot.resolve("classifier/src/main/kotlin")
    
    println("Output directory: ${classifierOutputDir.absolutePath}")
    println("Note: Normalization params (mean=${normParams.mean}, std=${normParams.std}) must be applied before detection")
    ClassifierCodeGenerator.generateCascadeClassifier(
        cascade,
        normParams.mean,
        normParams.std,
        "dev.yidafu.face.detection.generated",
        classifierOutputDir
    )
    println("✓ Classifier code generated successfully!")
    
    println("\n" + "=".repeat(80))
    println("Training completed successfully!")
    println("=".repeat(80))
}
