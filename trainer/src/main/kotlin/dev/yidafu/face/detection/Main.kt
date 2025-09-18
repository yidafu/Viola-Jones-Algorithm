package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.nio.file.Paths


suspend fun main() {
    // 加载图像文件路径

    val backgroundImages = loadImage(Paths.get(BACKGROUND_IMAGE_DIR))
    val faceImages = loadImage(Paths.get(BACKGROUND_IMAGE_DIR))

    val (xs, ys) = sampleDataNormalized(100, 100, faceImages, backgroundImages)
    val xis = xs.map { it.toIntegral() }

    // 创建特征
    val features = createAllFeatures(WINDOW_SIZE)
    // 创建弱分类器
    val (weakClassifiers, _) = buildWeakClassifiers("1st", 2, xis, ys.toList(), features)


    // 输出结果
    weakClassifiers.forEach { println(it) }
}
