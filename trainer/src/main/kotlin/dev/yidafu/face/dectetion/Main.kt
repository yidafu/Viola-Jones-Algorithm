package dev.yidafu.face.dectetion

import dev.yidafu.face.detection.toIntegral
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.nio.file.Paths


fun main() {
    val backgroundImages = loadImage(Paths.get(BACKGROUND_IMAGE_DIR))
    val faceImages = loadImage(Paths.get(BACKGROUND_IMAGE_DIR))

    val (xs, ys) = sampleDataNormalized(100, 100, faceImages, backgroundImages)
    val xis = xs.map { it.toIntegral() }

    val features = createAllFeatures()
    val (weak_classifiers, w_history )= buildWeakClassifiers("1st", 2, xis, ys.toList(), features)
}