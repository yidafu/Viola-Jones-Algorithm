package dev.yidafu.face.detection

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.random.Random
import kotlin.streams.toList

fun File.scale(tWidth: Int, tHeight: Int): BufferedImage {
    val sImg = ImageIO.read(this);
    return sImg.scale(tWidth, tHeight)
}


/**
 * 根据指定的模式匹配规则，在当前目录及其子目录中查找符合规则的文件
 *
 * @param pattern 用于匹配文件路径的模式字符串，遵循文件系统路径匹配语法
 * @return 符合匹配规则的文件路径列表
 */
fun File.glob(pattern: String): List<Path> {
    // 创建路径匹配器，用于根据模式匹配文件路径
    val matcher: PathMatcher = FileSystems.getDefault().getPathMatcher(pattern)
    // 遍历当前目录及其所有子目录，查找符合模式匹配规则的常规文件
    val files = Files.walk(this.toPath())
        .filter { Files.isRegularFile(it) && matcher.matches(it) }
        .toList().filterNotNull()
    return files

}


/**
 * 等效于 ImageOps.fit 的功能
 *
 * @param sourceImage 原始图像
 * @param minSize 目标正方形尺寸
 * @param backgroundColor 背景色（默认为透明）
 * @return 适配后的 BufferedImage
 */
fun BufferedImage.fit(
    minSize: Int,
): BufferedImage {
    require(minSize > 0) { "minSize must be positive" }

    // 创建缩放后的图像（使用高质量缩放）
    val width = this.width
    val height = this.height

    // 计算缩放比例，确保图像完整适配到正方形内
    val scale = minSize.toDouble() / maxOf(width, height)
    val scaledWidth = (width * scale).toInt()
    val scaledHeight = (height * scale).toInt()

    // 创建目标图像
    val resultImage = BufferedImage(minSize, minSize, BufferedImage.TYPE_INT_ARGB)


    val g2d = resultImage.createGraphics()
    // 设置高质量渲染提示
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    // 居中绘制缩放后的图像
    val x = (minSize - scaledWidth) / 2
    val y = (minSize - scaledHeight) / 2
    g2d.drawImage(this@fit, x, y, null)
    g2d.dispose()

    return resultImage
}
typealias OpenFileFunc = (Path, resize: Boolean) -> BufferedImage

/**
 * 将多个图片路径对应的图片合并成一张横向排列的图片
 *
 * @param resize 是否需要调整图片大小，默认为false
 * @param openFunc 打开文件的函数，用于从路径加载图片
 * @return 合并后的BufferedImage对象
 */
fun List<Path>.merge( resize: Boolean = false,
                      openFunc: OpenFileFunc,): BufferedImage {
    // 加载所有图片
    val images = this.map { openFunc(it, resize) }

    // 计算合并后图片的总宽度和最大高度
    val mergedWidth = images.sumOf { it.width }
    val mergedHeight = images.maxOf { it.height }

    // 创建合并后的图片画布
    val merged = BufferedImage(mergedWidth, mergedHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = merged.createGraphics()

    try {
        // 按顺序将每张图片绘制到合并画布上
        var xOffset = 0
        images.forEach { image ->
            graphics.drawImage(image, xOffset, 0, null)
            xOffset += image.width
        }
    } finally {
        // 释放图形资源
        graphics.dispose()
    }
    return merged
}

/**
 * 裁剪图像
 *
 * @param x 裁剪区域左上角的 x 坐标
 * @param y 裁剪区域左上角的 y 坐标
 * @param width 裁剪区域的宽度
 * @param height 裁剪区域的高度
 * @return 裁剪后的 BufferedImage
 */
fun BufferedImage.crop(x: Int, y: Int, width: Int, height: Int): BufferedImage {
    // 确保裁剪区域在图像范围内
    val safeX = x.coerceIn(0, this.width - 1)
    val safeY = y.coerceIn(0, this.height - 1)
    val safeWidth = width.coerceIn(1, this.width - safeX)
    val safeHeight = height.coerceIn(1, this.height - safeY)

    // 创建裁剪后的图像
    val cropped = BufferedImage(safeWidth, safeHeight, this.type)
    val g = cropped.createGraphics()
    g.drawImage(this, 0, 0, safeWidth, safeHeight, safeX, safeY, safeX + safeWidth, safeY + safeHeight, null)
    g.dispose()

    return cropped
}
/**
 * 对BufferedImage进行随机裁剪
 * @param minSize 裁剪的最小尺寸
 * @return 裁剪后的BufferedImage
 */
fun BufferedImage.randomCrop(minSize: Int): BufferedImage {
    // 计算允许的最大裁剪尺寸
    val maxAllowed = min(this.width, this.height)
    // 确保minSize不大于maxAllowed
    val safeMinSize = min(minSize, maxAllowed)
    // 生成随机裁剪尺寸
    val cropSize = if (safeMinSize >= maxAllowed) {
        maxAllowed // 如果最小尺寸大于等于最大尺寸，则使用最大尺寸
    } else {
        Random.nextInt(safeMinSize, maxAllowed + 1) // +1因为Random.nextInt是排他上限
    }
    
    // 计算最大偏移量
    val maxOffsetX = this.width - cropSize
    val maxOffsetY = this.height - cropSize
    
    // 随机选择裁剪起始位置
    val x = if (maxOffsetX <= 0) 0 else Random.nextInt(maxOffsetX + 1)
    val y = if (maxOffsetY <= 0) 0 else Random.nextInt(maxOffsetY + 1)

    // 执行裁剪操作
    return this.crop(x, y, cropSize, cropSize)
}

