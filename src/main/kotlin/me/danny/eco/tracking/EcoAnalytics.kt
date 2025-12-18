package me.danny.eco.tracking

import me.danny.eco.Config
import me.danny.eco.EcoPlugin
import me.danny.eco.Item
import me.danny.eco.gui.ViewerInventory
import org.bukkit.Bukkit
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.abs

internal val UNITS = TimeUnit.MINUTES
internal const val RESOLUTION = 15L
internal const val BINS_PER_DAY = 24 * (60 / RESOLUTION)

//How many days of history to keep
internal const val TOTAL_DAYS = 90
internal const val TOTAL_BINS = TOTAL_DAYS * BINS_PER_DAY

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
    private var analyticsTaskHandle: Int = -1

    class Data : Serializable {
        var lastTick: Long = Instant.now().toEpochMilli()
        val history: MutableMap<String, ItemVolumeHistory> = mutableMapOf()
    }

    val data: Data = Data()

    override fun load() {
        EcoPlugin.instance.logger.info("Volume analytics: Loading item history from ${file.absolutePath}")
        if (!file.exists()) file.createNewFile()
        try {
            val read = ObjectInputStream(GZIPInputStream(FileInputStream(file))).use(ObjectInputStream::readObject)
            if (read is Data) {
                data.lastTick = read.lastTick
                data.history.clear()
                for ((k, v) in read.history) {
                    data.history[k] = v
                }
            }
        } catch (_: Exception) {}

        val delta = deltaPerTick()
        val nextTick = calcNextTick()
        while (data.lastTick < nextTick) {
            data.history.values.forEach(ItemVolumeHistory::startNextBin)
            data.lastTick += delta
        }

        for (item in EcoPlugin.instance.worth.items) {
            data.history.putIfAbsent(item.name(), ItemVolumeHistory())
        }
        EcoPlugin.instance.logger.info("Volume analytics: Item history loaded.")
        analyticsTaskHandle = Bukkit.getScheduler().scheduleSyncRepeatingTask(EcoPlugin.instance,
            { tick() }, 0L, deltaPerTick() / 50)
    }

    override fun save() {
        if (!file.exists()) file.createNewFile()
        ObjectOutputStream(GZIPOutputStream(FileOutputStream(file))).use { obj -> obj.writeObject(data) }
        Bukkit.getScheduler().cancelTask(analyticsTaskHandle)
        analyticsTaskHandle = -1
    }

    override fun tick() {
        val now = Instant.now().toEpochMilli()
        if (abs(data.lastTick - now) < deltaPerTick()) return
        data.lastTick = calcNextTick()

        data.history.values.forEach(ItemVolumeHistory::startNextBin)
        ViewerInventory.refreshAll()
    }

    override fun allKeys(): Set<String> = data.history.keys

    override fun getHistory(query: String): ItemVolumeHistory =
        data.history.computeIfAbsent(query) { ItemVolumeHistory() }

    override fun log(item: Item, amount: Long) {
        getHistory(item.name()).add(amount)
        ViewerInventory.refreshAll()
    }

    private fun deltaPerTick(): Long = TimeUnit.MILLISECONDS.convert(RESOLUTION, UNITS)
    private fun calcNextTick(): Long = Instant.now().toEpochMilli() + deltaPerTick()
}