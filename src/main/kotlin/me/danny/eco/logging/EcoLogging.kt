package me.danny.eco.logging

import me.danny.eco.Config
import me.danny.eco.EcoPlugin
import me.danny.eco.Item
import org.bukkit.entity.Player
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private fun generateLogfileDate(): String = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE)

private fun formatDateTime(zdt: ZonedDateTime): String = zdt.format(DateTimeFormatter.ISO_DATE_TIME)

internal data class SaleRecord(val time: ZonedDateTime, val seller: UUID, val item: String, val amount: Long, val price: BigDecimal, val ext: BigDecimal) {
    companion object {
        internal fun fromCSV(csv: String): SaleRecord? {
            val parts = csv.split(",")
            try {
                val time = ZonedDateTime.parse(parts[0], DateTimeFormatter.ISO_DATE_TIME)
                val seller = UUID.fromString(parts[1])
                val item = parts[2] //not validated via Worth because the item might've been deleted since this record was created.
                val amount = parts[3].toLong()
                val price = BigDecimal(parts[4])
                val ext = BigDecimal(parts[5])

                return SaleRecord(time, seller, item, amount, price, ext)
            } catch (_: Exception) {
                if (!csv.isEmpty()) {
                    EcoPlugin.instance.logger.warning("[Logging] Invalid record, skipping: \"$csv\"")
                }
                return null
            }
        }
    }

    constructor(seller: UUID, item: Item, amount: Long, ext: BigDecimal) : this(ZonedDateTime.now(ZoneOffset.UTC), seller, item.name(), amount, item.worth, ext)

    internal fun toCSV(): String = "${formatDateTime(time)},$seller,$item,$amount,${price.toDouble()},${ext.toDouble()}"
}

internal sealed interface Logging {
    fun load() {}
    fun save() {}
    fun log(player: Player, item: Item, amount: Long, profit: BigDecimal) {}
    fun getLogs(): List<SaleRecord> = listOf()
}

internal fun getLogging(config: Config): Logging {
    return if (config.loggingEnabled) {
        EcoLogging()
    } else {
        DummyLogging
    }
}

private object DummyLogging : Logging

private class EcoLogging : Logging {
    private val file = EcoPlugin.instance.dataFolder.resolve("logs/sales-${generateLogfileDate()}.csv.gz")

    private val records: MutableList<SaleRecord> = mutableListOf()

    override fun load() {
        if (!EcoPlugin.instance.config.loggingPersistLogs) return
        file.parentFile.mkdirs()

        try {
            GZIPInputStream(FileInputStream(file)).bufferedReader()
                .use { it.readText() }
                .lines()
                .mapNotNull(SaleRecord::fromCSV)
                .forEach(records::add)
        } catch (_: FileNotFoundException) {}
    }

    override fun save() {
        if (!EcoPlugin.instance.config.loggingPersistLogs) return
        file.parentFile.mkdirs()

        try {
            GZIPOutputStream(FileOutputStream(file)).bufferedWriter()
                .use { bw ->
                    records.forEach {
                        bw.write(it.toCSV())
                        bw.newLine()
                    }
                }
        } catch (ex: IOException) {
            EcoPlugin.instance.logger.severe("An error occurred trying to save sale logs.")
            ex.printStackTrace()
        }
    }

    override fun log(player: Player, item: Item, amount: Long, profit: BigDecimal) {
        records.add(SaleRecord(player.uniqueId, item, amount, profit))
    }

    //Note: I'm aware that this is unsafe because the caller
    //could wipe the list. It's an internal function- either
    //someone is using reflection or I'm really messing up.
    override fun getLogs(): List<SaleRecord> = records
}