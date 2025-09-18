package dev.yidafu.face.detection

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.set
import java.awt.Image
import java.awt.image.BufferedImage

fun BufferedImage.scale(tWidth: Int, tHeight: Int): BufferedImage {

    val tmpImg = BufferedImage(tWidth, tHeight, type)

    val tmp = getScaledInstance(tWidth, tHeight, Image.SCALE_SMOOTH)
    val graphics = tmpImg.createGraphics()
    graphics.drawImage(tmp, 0, 0, null)
    graphics.dispose()
    return tmpImg
}


fun BufferedImage.toD3Array(): D3Array<Int> {
    val width = this.width
    val height = this.height
    val channels = 3 // 假设处理 RGB 三通道（忽略 alpha）
    val d3array = mk.zeros<Int>( height, width, channels)
    // 遍历每个像素
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this.getRGB(x, y);

            // 提取 RGB 通道（忽略 alpha）
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            d3array[y, x, 0] = r
            d3array[y, x, 1] = g
            d3array[y, x, 2] = b
        }
    }

    return d3array
}

fun BufferedImage.toD2Array(): D2Array<Int> {
    val width = this.width
    val height = this.height
    val d2array = mk.zeros<Int>( height, width)
    // 遍历每个像素
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this.getRGB(x, y);
            val b = pixel and 0xFF
            d2array[y, x] = b
        }
    }
    return d2array
}
