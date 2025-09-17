package dev.yidafu.face.dectetion

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get


abstract class Feature(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    abstract val coeffs: IntArray
    abstract val coordList: List<Pair<Int, Int>>
    fun sum(integralImage: D2Array<Float>): Float {
        return coordList.map { integralImage[it.second, it.first] }
            .mapIndexed { i, v ->
                v * coeffs[i]
            }.sum()
    }

    override fun toString(): String {
        return "${this::class.simpleName}{x=$x,y=$y,width=$width,height=$height}"
    }

    companion object {
        val Empty: Feature = object :Feature(0, 0, 0, 0) {
            override val coeffs: IntArray
                get() = TODO("Not yet implemented")
            override val coordList: List<Pair<Int, Int>>
                get() = TODO("Not yet implemented")

        }
    }

}

class Feature2h(
    x: Int,
    y: Int,
    wi: Int,
    he: Int,
) : Feature(x, y, width = wi, height = he) {
    val halfWidth = width / 2

    // hw = halfWidth
//        左侧矩形：
//        A(x, y)         B(x+hw, y)
//        C(x, y+height)  D(x+hw, y+height)
//
//        右侧矩形：
//        E(x+hw, y)      F(x+width, y)
//        G(x+hw, y+height) H(x+width, y+height)
    override val coordList = listOf<Pair<Int, Int>>(
        x to y,
        x + halfWidth to y,
        x to y + height,
        x + halfWidth to y + height,

        x + halfWidth to y,
        x + width to y,
        x + halfWidth to y + height,
        x + width to y + height,
    )

    //        特征值 = (D - B - C + A) - (H - F - G + E)
    //        = A - B - C + D - E + F + G - H
    override val coeffs: IntArray = intArrayOf(
        1, -1, -1, 1,
        -1, 1, 1, -1
    )
}

class Feature2v(
    x: Int,
    y: Int,
    wi: Int,
    he: Int,
) : Feature(x, y, wi, he) {
    override val coeffs: IntArray = intArrayOf(
        -1, 1, 1, -1,
        1, -1, -1, 1
    );
    val halfHeight = he / 2

    override val coordList = listOf(
        x to y,
        x + width to y,

        x to y + halfHeight,
        x + width to y + halfHeight,


        x to y + halfHeight,
        x + width to y + halfHeight,

        x to y + height,
        x + width to y + height,
    )
}



class Feature3h(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : Feature(x, y, width, height) {
    override val coeffs: IntArray = intArrayOf(
        -1, 1, 1, -1,
        1, -1, -1, 1,
        -1, 1, 1, -1
    )
    val trisectionWidth = width / 3
    override val coordList: List<Pair<Int, Int>> = listOf(
        x to y,
        x + trisectionWidth to y,
        x to y + height,
        x + trisectionWidth to y + height,

        x + trisectionWidth to y,
        x + 2 * trisectionWidth to y,
        x + trisectionWidth to y + height,
        x + 2 * trisectionWidth to y + height,

        x + 2 * trisectionWidth to y,
        x + width to y,
        x + 2 * trisectionWidth to y + height,
        x + width to y + height
    )
}


class Feature3v(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : Feature(x, y, width, height) {
    override val coeffs: IntArray = intArrayOf(
        -1, 1, 1, -1,
        1, -1, -1, 1,
        -1, 1, 1, -1
    )
    val trisectionHeight: Int = height / 3

    override val coordList: List<Pair<Int, Int>> = listOf(
        x to y,
        x + width to y,

        x to y + trisectionHeight,
        x + width to y + trisectionHeight,

        x to y + trisectionHeight,
        x + width to y + trisectionHeight,
        x to y + trisectionHeight * 2,
        x + width to y + trisectionHeight * 2,

        x to y + trisectionHeight * 2,
        x + width to y + trisectionHeight * 2,
        x to y + height,
        x + width to y + height,
    )
}

class Feature4(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : Feature(x, y, width, height) {
    override val coeffs: IntArray = intArrayOf(
        1, -1, -1, 1,
        -1, 1, 1, -1,
        -1, 1, 1, -1,
        1, -1, -1, 1
    )
    val hafWidth = width / 2
    val hafHeight = height / 2
    override val coordList: List<Pair<Int, Int>> = listOf(
        // upper row
        x to y,
        x + hafWidth to y,
        x to y + hafHeight,
        x + hafWidth to y + hafHeight,

        x + hafWidth to y,
        x + width to y,
        x + hafWidth to y + hafHeight,
        x + width to y + hafHeight,
        // upper row

        // lower row
        x to y + hafHeight,
        x + hafWidth to y + hafHeight,
        x to y + height,
        x + hafWidth to y + height,

        x + hafWidth to y + hafHeight,
        x + width to y + hafHeight,
        x + hafWidth to y + height,
        x + width to y + height
        // lower row
    )
}



data class Size(
    val width: Int,
    val height: Int,
)

data class Location(
    val x: Int,
    val y: Int,
)

fun possiblePosition(size: Int, windowSize: Int): List<Int> {
    return (0..(windowSize - size + 1)).toList()
}

fun possibleLocations(size: Size, windowSize: Int = WINDOW_SIZE): List<Location> {
    val list = mutableListOf<Location>()
    possiblePosition(size.width, windowSize).forEach { x ->
        possiblePosition(size.height, windowSize).forEach { y ->
            list.add(Location(x, y))
        }
    }
    return list
}

fun possibleShapes(size: Size, windowSize: Int= WINDOW_SIZE): List<Size> {
    val list = mutableListOf<Size>()
    (0..(windowSize - size.width + 1) step size.width).forEach { w ->
        (0..(windowSize - size.height + 1) step size.height).forEach { h ->
            val shape = Size(w, h)
            list.add(Size(w, h))
        }
    }
    return list
}



fun createFeatures(size: Size, block: (Location, Size) -> Feature): List<Feature> {
    return possibleShapes(size, WINDOW_SIZE).map { shape ->
        possibleLocations(shape, WINDOW_SIZE).map { loc ->
            block(loc, shape)
        }
    }.flatten()
}


fun createAllFeatures(): List<Feature> {
    val feature2v = createFeatures(Size(1, 2)) { loc, size ->
        Feature2v(loc.x, loc.y, size.width, size.height)
    }
    val feature2h = createFeatures(Size(2, 1)) { loc, size ->
        Feature2h(loc.x, loc.y, size.width, size.height)
    }
    val feature3v = createFeatures(Size(1, 3)) { loc, size ->
        Feature3v(loc.x, loc.y, size.width, size.height)
    }

    val feature3h = createFeatures(Size(3, 1)) { loc, size ->
        Feature3h(loc.x, loc.y, size.width, size.height)
    }

    val feature4 = createFeatures(Size(2, 2)) { loc, size ->
        Feature4(loc.x, loc.y, size.width, size.height)
    }

    val features = feature2v + feature2h + feature3v + feature3h + feature4

    return features
}

