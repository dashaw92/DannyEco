package me.danny.eco

import me.danny.eco.commands.ReloadWorth
import me.danny.eco.commands.SellCommand
import me.danny.eco.commands.SetWorthCommand
import me.danny.eco.commands.WorthCommand
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class EcoPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: EcoPlugin
    }

    internal val worthyml = dataFolder.resolve("worth.yml")
    internal lateinit var worth: Worth

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

        getCommand("sell")!!.setExecutor(SellCommand)
        getCommand("worth")!!.setExecutor(WorthCommand)
        getCommand("reloadworth")!!.setExecutor(ReloadWorth)
        getCommand("setworth")!!.setExecutor(SetWorthCommand)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ResetTask, 60L * 60L * 20L, 60L * 60L * 20L) //1 hour for real
//        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ResetTask, 5 * 20L, 5 * 20L) // 5 seconds for debugging
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