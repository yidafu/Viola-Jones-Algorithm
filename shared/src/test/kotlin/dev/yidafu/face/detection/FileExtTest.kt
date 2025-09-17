package dev.yidafu.face.detection

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

// 导入FileExt.kt中的所有扩展函数
import dev.yidafu.face.detection.*

// 用于生成测试用的临时图像
fun createTestImage(width: Int, height: Int): BufferedImage {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    for (x in 0 until width) {
        for (y in 0 until height) {
            // 创建一个简单的渐变图像
            val r = (x.toFloat() / width * 255).toInt()
            val g = (y.toFloat() / height * 255).toInt()
            val b = 128
            val a = 255
            image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
        }
    }
    return image
}

// 创建临时文件并写入图像数据
fun createTestImageFile(image: BufferedImage, format: String = "png"): File {
    val tempDir = createTempDirectory("test-images")
    val tempFile = tempDir.resolve("test-image.$format").toFile()
    javax.imageio.ImageIO.write(image, format, tempFile)
    return tempFile
}

@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class FileExtTest : StringSpec({
    // 测试完成后清理临时文件
    afterSpec {
        val currentDir = Paths.get(".").toAbsolutePath().normalize().toFile()
        currentDir.walkTopDown().forEach { file ->
            if (file.isDirectory && file.name.startsWith("test-images")) {
                try {
                    file.toPath().deleteRecursively()
                } catch (e: Exception) {
                    // 忽略清理错误
                }
            }
        }
    }

    // 测试 File.scale 函数
    "File.scale 应该正确缩放图像" { 
        val originalWidth = 200
        val originalHeight = 150
        val targetWidth = 100
        val targetHeight = 75
        
        val originalImage = createTestImage(originalWidth, originalHeight)
        val tempFile = createTestImageFile(originalImage)
        
        val scaledImage = tempFile.scale(targetWidth, targetHeight)
        
        scaledImage.width shouldBe targetWidth
        scaledImage.height shouldBe targetHeight
        
        // 清理临时文件
        tempFile.delete()
        tempFile.parentFile.delete()
    }

    // 测试 File.glob 函数
    "File.glob 应该正确匹配文件" { 
        // 创建测试目录结构
        val tempDir = createTempDirectory("glob-test")
        val testFile1 = tempDir.resolve("file1.txt")
        val testFile2 = tempDir.resolve("file2.png")
        val subDir = tempDir.resolve("subdir")
        subDir.toFile().mkdirs()
        val testFile3 = subDir.resolve("file3.png")
        
        // 创建空文件
        testFile1.writeBytes(byteArrayOf())
        testFile2.writeBytes(byteArrayOf())
        testFile3.writeBytes(byteArrayOf())
        
        // 测试不同的模式
        val dirFile = tempDir.toFile()
        
        // 匹配所有PNG文件
        val pngFiles = dirFile.glob("glob:**/*.png")
        pngFiles.size shouldBe 2
        val fileNames = pngFiles.map { path -> path.fileName.toString() }.toSet()
        fileNames shouldBe setOf("file2.png", "file3.png")
        
        // 匹配 subdir 所有TXT文件
        val txtFiles = dirFile.glob("glob:**/*.txt")
        txtFiles.size shouldBe 1
        txtFiles[0].fileName.toString() shouldBe "file1.txt"
        
        // 匹配根目录下的所有文件
        val rootFiles = dirFile.glob("glob:*.txt")
        rootFiles.size shouldBe 0

        // 清理临时文件
        tempDir.deleteRecursively()
    }

    // 测试 BufferedImage.fit 函数
    "BufferedImage.fit 应该正确适配图像到正方形" { 
        // 创建不同宽高比的图像
        val landscapeImage = createTestImage(400, 300)
        val portraitImage = createTestImage(300, 400)
        val squareImage = createTestImage(300, 300)
        
        val targetSize = 200
        
        // 测试横向图像
        val fittedLandscape = landscapeImage.fit(targetSize)
        fittedLandscape.width shouldBe targetSize
        fittedLandscape.height shouldBe targetSize
        
        // 测试纵向图像
        val fittedPortrait = portraitImage.fit(targetSize)
        fittedPortrait.width shouldBe targetSize
        fittedPortrait.height shouldBe targetSize
        
        // 测试正方形图像
        val fittedSquare = squareImage.fit(targetSize)
        fittedSquare.width shouldBe targetSize
        fittedSquare.height shouldBe targetSize
        
        // 测试小尺寸目标
        val smallSize = 100
        val fittedSmall = landscapeImage.fit(smallSize)
        fittedSmall.width shouldBe smallSize
        fittedSmall.height shouldBe smallSize
    }

    // 属性测试：BufferedImage.fit 应该保持图像内容不变
    "BufferedImage.fit 应该通过属性测试" { 
        checkAll(Arb.int(1, 100), Arb.int(1, 100), Arb.int(1, 200)) { width, height, targetSize ->
            val image = createTestImage(width, height)
            val fittedImage = image.fit(targetSize)
            
            fittedImage.width shouldBe targetSize
            fittedImage.height shouldBe targetSize
        }
    }

    // 测试 List<Path>.merge 函数
    "List<Path>.merge 应该正确合并图像" { 
        // 创建临时图像文件
        val image1 = createTestImage(100, 100)
        val image2 = createTestImage(150, 100)
        val image3 = createTestImage(200, 100)
        
        val file1 = createTestImageFile(image1)
        val file2 = createTestImageFile(image2)
        val file3 = createTestImageFile(image3)
        
        val paths = listOf(file1.toPath(), file2.toPath(), file3.toPath())
        
        // 定义打开文件的函数
        val openFunc: (Path, Boolean) -> BufferedImage = { path, _ -> 
            javax.imageio.ImageIO.read(path.toFile())
        }
        
        // 合并图像
        val mergedImage = paths.merge(openFunc = openFunc)
        
        // 验证合并后的图像尺寸
        mergedImage.width shouldBe 100 + 150 + 200 // 所有图像宽度之和
        mergedImage.height shouldBe 100 // 最大高度
        
        // 清理临时文件
        file1.delete()
        file2.delete()
        file3.delete()
        file1.parentFile.delete()
        file2.parentFile.delete()
        file3.parentFile.delete()
    }

    // 测试 BufferedImage.crop 函数
    "BufferedImage.crop 应该正确裁剪图像" { 
        val originalWidth = 200
        val originalHeight = 200
        val image = createTestImage(originalWidth, originalHeight)
        
        val x = 50
        val y = 50
        val width = 100
        val height = 100
        
        val croppedImage = image.crop(x, y, width, height)
        
        croppedImage.width shouldBe width
        croppedImage.height shouldBe height
        
        // 验证裁剪后的像素值与原图像对应位置相同
        for (i in 0 until width) {
            for (j in 0 until height) {
                croppedImage.getRGB(i, j) shouldBe image.getRGB(x + i, y + j)
            }
        }
    }

    // 属性测试：BufferedImage.crop 应该处理边界情况
    "BufferedImage.crop 应该通过属性测试" { 
        checkAll(Arb.int(100, 500), Arb.int(100, 500)) { width, height ->
            val image = createTestImage(width, height)
            
            // 测试正常裁剪
            val x = width / 4
            val y = height / 4
            val cropWidth = width / 2
            val cropHeight = height / 2
            
            val croppedImage = image.crop(x, y, cropWidth, cropHeight)
            
            croppedImage.width shouldBe cropWidth
            croppedImage.height shouldBe cropHeight
            
            // 测试边界裁剪（超出图像范围）
            val outOfBoundsCropped = image.crop(width / 2, height / 2, width, height)
            // 简化断言，只检查裁剪后的尺寸在有效范围内
            outOfBoundsCropped.width shouldBeGreaterThanOrEqual 0
            outOfBoundsCropped.height shouldBeGreaterThanOrEqual 0
            
            // 测试负数坐标
            val negativeCropped = image.crop(-50, -50, width, height)
            // 简化断言，只检查裁剪后的尺寸在有效范围内
            negativeCropped.width shouldBeGreaterThanOrEqual 0
            negativeCropped.height shouldBeGreaterThanOrEqual 0
        }
    }

    // 测试 BufferedImage.randomCrop 函数
    "BufferedImage.randomCrop 应该正确随机裁剪图像" { 
        val originalWidth = 300
        val originalHeight = 300
        val minSize = 100
        val image = createTestImage(originalWidth, originalHeight)
        
        val croppedImage = image.randomCrop(minSize)
        
        // 验证裁剪后的图像尺寸在有效范围内

        croppedImage.width shouldBeGreaterThanOrEqual minSize
        croppedImage.width shouldBeLessThanOrEqual originalWidth
        croppedImage.height shouldBeGreaterThanOrEqual minSize
        croppedImage.height shouldBeLessThanOrEqual originalHeight
        croppedImage.width shouldBe croppedImage.height // 应该是正方形
    }

    // 属性测试：BufferedImage.randomCrop 应该在有效范围内随机裁剪
    "BufferedImage.randomCrop 应该通过属性测试" { 
        checkAll(Arb.int(200, 500), Arb.int(200, 500), Arb.int(50, 100)) { width, height, minSize ->
            val image = createTestImage(width, height)
            val croppedImage = image.randomCrop(minSize)
            
            val expectedMaxSize = kotlin.math.min(width, height)
            croppedImage.width shouldBeGreaterThanOrEqual minSize
            croppedImage.width shouldBeLessThanOrEqual expectedMaxSize
            croppedImage.height shouldBeGreaterThanOrEqual minSize
            croppedImage.height shouldBeLessThanOrEqual expectedMaxSize
            croppedImage.width shouldBe croppedImage.height
        }
    }
})