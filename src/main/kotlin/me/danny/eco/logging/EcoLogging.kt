package me.danny.eco.logging

import me.danny.eco.Config
import me.danny.eco.EcoPlugin
import me.danny.eco.Item
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.time.ZonedDateTime

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

    private lateinit var currentLog: EcoLogMgr.EcoLog

    override fun load() {
        if (!EcoPlugin.instance.config.loggingPersistLogs) return
        currentLog = EcoLogMgr.load()
    }

    override fun save() {
        if (!EcoPlugin.instance.config.loggingPersistLogs) return
        EcoLogMgr.save(currentLog)
    }

    override fun log(player: Player, item: Item, amount: Long, profit: BigDecimal) {
        currentLog.records.add(SaleRecord(player.uniqueId, item, amount, profit))
    }

    //Note: I'm aware that this is unsafe because the caller
    //could wipe the list. It's an internal function- either
    //someone is using reflection or I'm really messing up.
    override fun getLogs(): List<SaleRecord> = currentLog.records
}