package me.danny.eco.tracking

import me.danny.eco.Config
import me.danny.eco.EcoPlugin
import me.danny.eco.Item
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

//How many hours for 1 bin
const val RESOLUTION = 1L
const val BINS_PER_DAY = 24 * RESOLUTION

//How many days of history to keep
const val TOTAL_DAYS = 365
const val TOTAL_BINS = TOTAL_DAYS * BINS_PER_DAY

internal sealed interface Analytics {
    fun load() {}
    fun save() {}
    fun log(item: Item, amount: Long) {}
    fun tick() {}
    fun allKeys(): Set<String> = setOf()
    fun getHistory(query: String): ItemVolumeHistory? = null
}

internal fun getAnalytics(config: Config): Analytics {
    return if (config.ecoAnalyticsEnabled) {
        EcoAnalytics()
    } else {
        DummyAnalytics
    }
}

private object DummyAnalytics : Analytics

private class EcoAnalytics : Analytics {

    private val file = EcoPlugin.instance.dataFolder.resolve("item-history.bin")

    class Data : Serializable {
        val history: MutableMap<String, ItemVolumeHistory> = mutableMapOf()
    }

    val data: Data = Data()
    private var nextTick: Long = calcNextTick()

    override fun load() {
        EcoPlugin.instance.logger.info("Volume analytics: Loading item history from ${file.absolutePath}")
        if (!file.exists()) file.createNewFile()
        try {
            val read = ObjectInputStream(GZIPInputStream(FileInputStream(file))).use(ObjectInputStream::readObject)
            if (read is Data) {
                data.history.clear()
                for ((k, v) in read.history) {
                    data.history[k] = v
                }
            }
        } catch (_: Exception) {
        }

        for (item in EcoPlugin.instance.worth.items) {
            data.history.putIfAbsent(item.name(), ItemVolumeHistory())
        }
        EcoPlugin.instance.logger.info("Volume analytics: Item history loaded.")
    }

    override fun save() {
        if (!file.exists()) file.createNewFile()
        ObjectOutputStream(GZIPOutputStream(FileOutputStream(file))).use { obj -> obj.writeObject(data) }
    }

    override fun tick() {
        val now = Instant.now().toEpochMilli()
        if (now < nextTick) return
        nextTick = calcNextTick()

        data.history.values.forEach(ItemVolumeHistory::startNextBin)
    }

    override fun allKeys(): Set<String> = data.history.keys

    override fun getHistory(query: String): ItemVolumeHistory =
        data.history.computeIfAbsent(query) { ItemVolumeHistory() }

    override fun log(item: Item, amount: Long) {
        getHistory(item.name()).add(amount)
    }

    private fun calcNextTick(): Long =
        Instant.now().toEpochMilli() + TimeUnit.MILLISECONDS.convert(RESOLUTION, TimeUnit.HOURS)
}