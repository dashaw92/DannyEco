package me.danny.eco

import me.danny.eco.commands.BinCommand
import me.danny.eco.commands.EcoLogCommand
import me.danny.eco.commands.ReloadConfig
import me.danny.eco.commands.ReloadWorth
import me.danny.eco.commands.SellCommand
import me.danny.eco.commands.SetWorthCommand
import me.danny.eco.commands.WorthCommand
import me.danny.eco.gui.GuiListener
import me.danny.eco.gui.ViewerInventory
import me.danny.eco.logging.Logging
import me.danny.eco.logging.getLogging
import me.danny.eco.tracking.Analytics
import me.danny.eco.tracking.getAnalytics
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class EcoPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: EcoPlugin
    }

    internal val worthyml = dataFolder.resolve("worth.yml")
    internal lateinit var config: Config
    internal lateinit var ecolog: Logging
    internal lateinit var worth: Worth
    internal lateinit var analytics: Analytics
    private var resetTaskHandle: Int = -1

    override fun onLoad() {
        instance = this
        dataFolder.mkdirs()
    }

    override fun onEnable() {
        if (!hasEconomy()) {
            logger.severe("Vault plugin not found.")
            isEnabled = false
            return
        }

        logger.info("Loading worth.yml from $worthyml")
        worth = Worth.loadFromFile(worthyml)

        configReload()

        getCommand("sell")!!.setExecutor(SellCommand)
        getCommand("worth")!!.setExecutor(WorthCommand)
        getCommand("reloadworth")!!.setExecutor(ReloadWorth)
        getCommand("reloadconfig")!!.setExecutor(ReloadConfig)
        getCommand("setworth")!!.setExecutor(SetWorthCommand)
        getCommand("ecolog")!!.setExecutor(EcoLogCommand)

        getCommand("bin")!!.setExecutor(BinCommand)
        startTask()

        Bukkit.getPluginManager().registerEvents(GuiListener, this)
    }

    override fun onDisable() {
        ViewerInventory.onDisable()
        analytics.save()
        ecolog.save()
    }

    internal fun configReload() {
        logger.info("Loading config.yml")
        config = Config.loadFromFile()

        analytics = getAnalytics(config)
        analytics.load()

        ecolog = getLogging(config)
        ecolog.load()

        startTask()
    }

    internal fun startTask() {
        if (resetTaskHandle != -1) {
            Bukkit.getScheduler().cancelTask(resetTaskHandle)
        }

        resetTaskHandle = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ResetTask, 0L, config.resetDelayTicks)
    }
}

internal fun CommandSender.msg(msg: String)
{
    sendMessage("&e$msg".color())
}

internal fun CommandSender.msgErr(msg: String) {
    sendMessage("&7$msg".color())
}

internal fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)