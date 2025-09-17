package dev.yidafu.face.detection

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// 确保导入扩展函数

class ListExtTest : StringSpec({
    "sample 应该返回指定大小的随机样本" {
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val sampleSize = 3
        val sample = list.sample(sampleSize)
        
        sample.size shouldBe sampleSize
        sample.forEach { it shouldBeIn list }
        // 验证样本中的元素是唯一的
        sample.toSet().size shouldBe sampleSize
    }

    "sample 应该在 n=0 时返回空列表" {
        val list = listOf(1, 2, 3)
        list.sample(0) shouldBe emptyList()
    }

    "sample 应该在 n等于列表大小时返回原始列表的一个排列" {
        val list = listOf(1, 2, 3, 4)
        val sample = list.sample(4)
        
        sample.size shouldBe list.size
        sample shouldContainAll list
    }

    "sample 应该在 n为负数时抛出异常" {
        val list = listOf(1, 2, 3)
        shouldThrow<IllegalArgumentException> { 
            list.sample(-1)
        }.message shouldBe "Sample size must be non-negative"
    }

    "sample 应该在 n大于列表大小时抛出异常" {
        val list = listOf(1, 2, 3)
        shouldThrow<IllegalArgumentException> { 
            list.sample(5)
        }.message shouldBe "Sample size must not exceed list size"
    }

    "sample 应该通过属性测试" {
        checkAll(Arb.list(Arb.int(), 1..100), Arb.int(0..100)) { list, n ->
            if (n <= list.size) {
                val sample = list.sample(n)
                sample.size shouldBe n
                sample.all { it in list } shouldBe true
            } else {
                shouldThrow<IllegalArgumentException> { list.sample(n) }
            }
        }
    }

    "zip 三个列表应该返回三元组列表" {
        val a = listOf(1, 2, 3)
        val b = listOf("a", "b", "c")
        val c = listOf(true, false, true)
        
        val result = a.zip(b, c)
        
        result.size shouldBe 3
        result shouldBe listOf(
            Triple(1, "a", true),
            Triple(2, "b", false),
            Triple(3, "c", true)
        )
    }

    "zip 三个列表应该在长度不同时截断到最短列表的长度" {
        val a = listOf(1, 2, 3, 4)
        val b = listOf("a", "b")
        val c = listOf(true, false, true)
        
        val result = a.zip(b, c)
        
        result.size shouldBe 2
        result shouldBe listOf(
            Triple(1, "a", true),
            Triple(2, "b", false)
        )
    }

    "zip 三个空列表应该返回空列表" {
        val a = emptyList<Int>()
        val b = emptyList<String>()
        val c = emptyList<Boolean>()
        
        a.zip(b, c) shouldBe emptyList()
    }

    "reorder 应该根据索引数组重新排序列表" {
        val list = listOf(10, 20, 30, 40, 50)
        val indices = intArrayOf(3, 0, 4, 1, 2)
        
        val result = list.reorder(indices)
        
        result shouldBe listOf(40, 10, 50, 20, 30)
    }

    "reorder 应该在索引数组包含所有原始索引时保持列表大小不变" {
        checkAll(Arb.list(Arb.string(), 1..10), Arb.int(1..10)) { list, _ ->
            val shuffledIndices = list.indices.shuffled().toIntArray()
            val result = list.reorder(shuffledIndices)
            
            result.size shouldBe list.size
            result.toSet() shouldBe list.toSet()
        }
    }

    "reorder 应该在索引数组与原索引相同顺序时返回原始列表" {
        val list = listOf(1, 2, 3, 4)
        val indices = intArrayOf(0, 1, 2, 3)
        
        list.reorder(indices) shouldBe list
    }

    "argsort 应该按升序返回排序后的列表和索引" {
        val list = listOf(5, 2, 9, 1, 7)
        val (sortedList, indices) = list.argsort(ascending = true)
        
        sortedList shouldBe listOf(1, 2, 5, 7, 9)
        indices shouldBe intArrayOf(3, 1, 0, 4, 2)
        
        // 验证索引数组的正确性
        indices.indices.forEach { i ->
            sortedList[i] shouldBe list[indices[i]]
        }
    }

    "argsort 应该按降序返回排序后的列表和索引" {
        val list = listOf(5, 2, 9, 1, 7)
        val (sortedList, indices) = list.argsort(ascending = false)
        
        sortedList shouldBe listOf(9, 7, 5, 2, 1)
        indices shouldBe intArrayOf(2, 4, 0, 1, 3)
        
        // 验证索引数组的正确性
        indices.indices.forEach { i ->
            sortedList[i] shouldBe list[indices[i]]
        }
    }

    "argsort 应该在默认情况下按升序排序" {
        val list = listOf(3, 1, 4, 1, 5, 9)
        val (sortedList, _) = list.argsort()
        
        sortedList shouldBe list.sorted()
    }

    "argsort 应该正确处理相同元素的情况" {
        val list = listOf(2, 2, 1, 1, 3)
        val (sortedList, indices) = list.argsort()
        
        sortedList shouldBe listOf(1, 1, 2, 2, 3)
        
        // 验证索引数组的正确性
        indices.indices.forEach { i ->
            sortedList[i] shouldBe list[indices[i]]
        }
    }

    "argsort 应该通过属性测试" {
        checkAll(Arb.list(Arb.int(), 1..100)) { list ->
            val (sortedListAsc, indicesAsc) = list.argsort(ascending = true)
            val (sortedListDesc, indicesDesc) = list.argsort(ascending = false)
            
            // 验证升序排序
            sortedListAsc shouldBe list.sorted()
            indicesAsc.indices.forEach { i ->
                sortedListAsc[i] shouldBe list[indicesAsc[i]]
            }
            
            // 验证降序排序
            sortedListDesc shouldBe list.sortedDescending()
            indicesDesc.indices.forEach { i ->
                sortedListDesc[i] shouldBe list[indicesDesc[i]]
            }
        }
    }
})