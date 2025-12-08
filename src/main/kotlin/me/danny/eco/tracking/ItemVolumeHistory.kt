package me.danny.eco.tracking

import java.io.Serializable

//How many hours for 1 bin
private const val RESOLUTION = 1
private const val BINS_PER_DAY = 24 * RESOLUTION
//How many days of history to keep
private const val TOTAL_DAYS = 30
private const val TOTAL_BINS = TOTAL_DAYS * BINS_PER_DAY

internal class ItemVolumeHistory(val bins: LongArray = LongArray(TOTAL_BINS) { it.toLong() }) : Serializable {

    fun startNextBin() {
        bins.shift(1)
    }

    fun add(amount: Long) {
        bins[0] += amount
    }

    fun getFirstNDays(days: Int) : List<Long> {
        val clampedDays = days.coerceIn(1, TOTAL_DAYS)
        val numBins = clampedDays * BINS_PER_DAY
        return bins.slice(0 until numBins)
    }

    fun averagePerDay() : Long = bins.sum() / TOTAL_DAYS
}

// [1, 2, 3, ..., n].shift(1) -> [0, 1, 2, 3, ...]
private fun LongArray.shift(amount: Int) {
    val n = amount.coerceIn(0, size - 1)
    if (n == 0) return

    val retained = sliceArray(0 until size - n)

    for (i in n until size) {
        this[i] = retained[i - n]
    }

    for (i in 0 until n) {
        this[i] = 0
    }
}