package dev.yidafu.face.detection

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.jetbrains.kotlinx.multik.ndarray.data.get
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.min

class BufferedImageExtTest : StringSpec({
    // 用于生成测试用的图像
    fun createTestImage(width: Int, height: Int, color: Color = Color.WHITE): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                image.setRGB(x, y, color.rgb)
            }
        }
        return image
    }

    // 用于生成具有特定颜色分布的测试图像
    fun createTestImageWithPattern(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = (x * 255) / (width - 1)
                val g = (y * 255) / (height - 1)
                val b = ((x + y) * 255) / (width + height - 2)
                image.setRGB(x, y, Color(r, g, b).rgb)
            }
        }
        return image
    }

    // 测试 BufferedImage.scale 函数
    "BufferedImage.scale 应该正确缩放图像" {
        val originalWidth = 300
        val originalHeight = 200
        val image = createTestImage(originalWidth, originalHeight)
        
        // 测试放大
        val scaledUp = image.scale(600, 400)
        scaledUp.width shouldBe 600
        scaledUp.height shouldBe 400
        
        // 测试缩小
        val scaledDown = image.scale(150, 100)
        scaledDown.width shouldBe 150
        scaledDown.height shouldBe 100
        
        // 测试不同比例的缩放
        val differentRatio = image.scale(200, 300)
        differentRatio.width shouldBe 200
        differentRatio.height shouldBe 300
    }

    // 属性测试：BufferedImage.scale 应该通过各种尺寸的测试
    "BufferedImage.scale 应该通过属性测试" {
        checkAll(Arb.int(10, 1000), Arb.int(10, 1000), Arb.int(5, 2000), Arb.int(5, 2000)) {
            originalWidth, originalHeight, targetWidth, targetHeight ->
            val image = createTestImage(originalWidth, originalHeight)
            val scaled = image.scale(targetWidth, targetHeight)
            scaled.width shouldBe targetWidth
            scaled.height shouldBe targetHeight
        }
    }

    // 测试 BufferedImage.toD3Array 函数
    "BufferedImage.toD3Array 应该正确转换为3D数组" {
        val width = 2
        val height = 2
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        
        // 设置特定的像素值以便验证
        image.setRGB(0, 0, Color(255, 0, 0).rgb) // 红色
        image.setRGB(1, 0, Color(0, 255, 0).rgb) // 绿色
        image.setRGB(0, 1, Color(0, 0, 255).rgb) // 蓝色
        image.setRGB(1, 1, Color(255, 255, 255).rgb) // 白色
        
        val d3Array = image.toD3Array()
        
        // 验证数组维度
        d3Array.shape shouldBe intArrayOf(height, width, 3)
        
        // 验证像素值（归一化后的值）
        d3Array[0, 0, 0] shouldBe 1 // 红色通道
        d3Array[0, 0, 1] shouldBe 0 // 绿色通道
        d3Array[0, 0, 2] shouldBe 0 // 蓝色通道
        
        d3Array[0, 1, 0] shouldBe 0 // 红色通道
        d3Array[0, 1, 1] shouldBe 1 // 绿色通道
        d3Array[0, 1, 2] shouldBe 0 // 蓝色通道
        
        d3Array[1, 0, 0] shouldBe 0 // 红色通道
        d3Array[1, 0, 1] shouldBe 0 // 绿色通道
        d3Array[1, 0, 2] shouldBe 1 // 蓝色通道
        
        d3Array[1, 1, 0] shouldBe 1 // 红色通道
        d3Array[1, 1, 1] shouldBe 1 // 绿色通道
        d3Array[1, 1, 2] shouldBe 1 // 蓝色通道
    }

    // 属性测试：BufferedImage.toD3Array 应该正确处理各种尺寸的图像
    "BufferedImage.toD3Array 应该通过属性测试" {
        checkAll(Arb.int(5, 100), Arb.int(5, 100)) { width, height ->
            val image = createTestImageWithPattern(width, height)
            val d3Array = image.toD3Array()
            
            // 验证数组维度
            d3Array.shape shouldBe intArrayOf(height, width, 3)
            
            // 验证部分像素值
            for (y in 0 until min(height, 5)) {
                for (x in 0 until min(width, 5)) {
                    val pixel = image.getRGB(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    
                    d3Array[y, x, 0] shouldBe r / 255
                    d3Array[y, x, 1] shouldBe g / 255
                    d3Array[y, x, 2] shouldBe b / 255
                }
            }
        }
    }

    // 测试 BufferedImage.toD2Array 函数
    "BufferedImage.toD2Array 应该正确转换为2D数组" {
        val width = 2
        val height = 2
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        
        // 设置特定的像素值以便验证（只关注蓝色通道）
        image.setRGB(0, 0, Color(0, 0, 0).rgb) // 黑色
        image.setRGB(1, 0, Color(0, 0, 128).rgb) // 半蓝
        image.setRGB(0, 1, Color(0, 0, 255).rgb) // 蓝色
        image.setRGB(1, 1, Color(255, 255, 255).rgb) // 白色
        
        val d2Array = image.toD2Array()
        
        // 验证数组维度
        d2Array.shape shouldBe intArrayOf(height, width)
        
        // 验证像素值（归一化后的值）
        d2Array[0, 0] shouldBe 0 // 黑色
        d2Array[0, 1] shouldBe 0 // 半蓝 (128/255 = 0 整数除法)
        d2Array[1, 0] shouldBe 1 // 蓝色 (255/255 = 1)
        d2Array[1, 1] shouldBe 1 // 白色
    }

    // 属性测试：BufferedImage.toD2Array 应该正确处理各种尺寸的图像
    "BufferedImage.toD2Array 应该通过属性测试" {
        checkAll(Arb.int(5, 100), Arb.int(5, 100)) { width, height ->
            val image = createTestImageWithPattern(width, height)
            val d2Array = image.toD2Array()
            
            // 验证数组维度
            d2Array.shape shouldBe intArrayOf(height, width)
            
            // 验证部分像素值
            for (y in 0 until min(height, 5)) {
                for (x in 0 until min(width, 5)) {
                    val pixel = image.getRGB(x, y)
                    val b = pixel and 0xFF
                    
                    d2Array[y, x] shouldBe b / 255
                }
            }
        }
    }
})