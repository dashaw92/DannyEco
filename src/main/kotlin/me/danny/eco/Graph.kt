package me.danny.eco

import kotlin.math.floor

object Graph {
    fun create(
        width: Int = 60,
        height: Int = 14,
        points: List<Long>
    ): List<String> {
        val axisBorder = "&7▇"
        val blank = "&8╱"
        val full = "&2▇"
        val peak = "&a▃"

        val yDiv = points.max() / (height + 1).toDouble()
        return height.downTo(0).map { y ->
            (0..width).joinToString("") { x ->
                if (y == 0 || x == 0) axisBorder
                else {
                    val bin = points[floor(x / width.toDouble() * points.size - 1).toInt()]

                    if (bin > yDiv * y) full
                    else if (bin > (yDiv * (y - 0.5))) peak
                    else blank
                }
            }.color()
        }.toList()
    }
}