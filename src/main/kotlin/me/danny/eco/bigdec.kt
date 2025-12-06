package me.danny.eco

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal fun tryParseBigDec(str: String?) : BigDecimal? {
    if (str == null) return null
    return try {
        BigDecimal(str)
    } catch (_: Exception) {
        null
    }
}

internal fun format(bd: BigDecimal) : String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    return fmt.format(bd)
}

internal fun BigDecimal.fmt() : String = format(this)