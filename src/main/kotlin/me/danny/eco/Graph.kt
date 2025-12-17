package me.danny.eco

import kotlin.math.floor
import kotlin.math.round

object Graph {
    fun create(
        width: Int = 58,
        height: Int = 14,
        points: List<Long>,
        timescale: String
    ): List<String> {
        val axisBorder = "&7▇"
        val blank = "&8╱"
        val full = "&2▇"
        val peak = "&a▃"

        val binsPerDiv = ((points.size - 1) / (width - 1).toDouble())

        val yDiv = points.max() / (height + 1).toDouble()
        val graph = height.downTo(0).map { y ->
            val line = (0..width).joinToString("") { x ->
                if (y == 0 || x == 0) axisBorder
                else {
                    val bin = points[floor((x - 1) * binsPerDiv).toInt()]

                    if (bin > yDiv * y) full
                    else if (bin > (yDiv * (y - 0.5))) peak
                    else blank
                }
            }

            val suffix =
                if (y == height) "&6${fmtAsK(points.max().toDouble())}".color()
                else if (y == 1) "&70".color()
                else if (y != 0 && y % 5 == 0) "&7${fmtAsK(y * yDiv)}".color()
                else ""
            "$line $suffix".color()
        }.toMutableList()

        graph.add("&7&oX Scale: ${round(binsPerDiv * 100.0) / 100.0} $timescale per division.".color())
        graph.add("&7&oY Scale: ${fmtAsK(yDiv)} units sold per division.".color())
        return graph
    }
}

private fun fmtAsK(x: Double): String {
    return if (x >= 1000.0) "%.2fk".format(round(x) / 1000.0)
    else "${round(x * 100.0) / 100.0}"
}