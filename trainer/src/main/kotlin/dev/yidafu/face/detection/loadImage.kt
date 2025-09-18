package dev.yidafu.face.detection

import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.min

fun loadImage(filePath: Path): List<Path> {
    val files = filePath.toFile().glob("glob:**/*.{png,jpg}")
    return files
}


val openFace: OpenFileFunc = { path, resize ->
    val image = ImageIO.read(path.toFile()).apply {
        crop(0, CROP_TOP, width, height - CROP_TOP)
    }.toD3Array().gleam().toBufferedImage()
    val size = min(image.width, image.height)
    val fitImage = image.fit(size)

    if (resize) {
        fitImage.scale(WINDOW_SIZE, WINDOW_SIZE)
    } else {
        fitImage
    }
}


val openBackground: OpenFileFunc = { path, resize ->
    val bgImg = ImageIO.read(path.toFile()).toD3Array().gleam().toBufferedImage()
    val croppedBgImg = bgImg.randomCrop(WINDOW_SIZE)

    if (resize) {
        croppedBgImg.scale(WINDOW_SIZE, WINDOW_SIZE)
    } else {
        croppedBgImg
    }
}

fun loadBackgroundImages(path: Path, resize: Boolean = true): List<BufferedImage> {
    return loadImage(path).map { openBackground(it, resize) }
}


fun loadFaceImages(path: Path, resize: Boolean = true): List<BufferedImage> {
    return loadImage(path).map { openFace(it, resize) }
}
