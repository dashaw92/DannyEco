package me.danny.eco.commands

import me.danny.eco.tracking.EcoAnalytics
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

object BinCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (args.size != 1) return false
        val hist = EcoAnalytics.history[args[0]]
        sender.sendMessage("${args[0]} bins: ${hist?.bins?.joinToString(separator = ", ")}")
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String?> {
        return EcoAnalytics.history.keys.toList()
    }
}