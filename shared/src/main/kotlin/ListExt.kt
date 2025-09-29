package dev.yidafu.face.detection

import kotlin.random.Random


fun <T> List<T>.sample(n: Int): List<T> {
    require(n >= 0) { "Sample size must be non-negative" }
    require(n <= size) { "Sample size must not exceed list size" }
    if (n <= size) {
        return shuffled().take(n)
    }
    return (0..<n).map {
        val index = Random(1000).nextInt(0, size)
        this[index]
    }
}

fun <A, B, C> List<A>.zip(b: List<B>, c: List<C>): List<Triple<A, B, C>> {
    return this.zip(b).zip(c) { pair, third ->
        Triple(pair.first, pair.second, third)
    }
}


/**
 * 使用给定的索引数组重新排序
 */
inline fun <reified T> List<T>.reorder(indices: IntArray): List<T> {
    return List(this.size) { i -> this[indices[i]] }
}

/**
 * 对数组进行 argsort 排序
 *
 * @return 排序后的数组和索引数组
 */
fun <T: Number> List<T>.argsort(ascending: Boolean = true): Pair<List<T>, IntArray> {
    // 创建索引数组
    val indices = this.indices.toList()

    // 根据排序方向排序索引
    val sortedIndices = if (ascending) {
        indices.sortedBy { this[it].toDouble() }
    } else {
        indices.sortedByDescending { this[it] .toDouble()}
    }.toIntArray()

    // 使用索引排序数组
    val sortedArray = List<T>(this.size) { i -> this[sortedIndices[i]] }

    return sortedArray to sortedIndices
}
