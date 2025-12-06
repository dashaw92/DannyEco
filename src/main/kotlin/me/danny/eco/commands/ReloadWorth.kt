package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.Worth
import me.danny.eco.msg
import me.danny.eco.msgErr
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ReloadWorth : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (!sender.hasPermission("dannyeco.admin")) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        EcoPlugin.instance.worth = Worth.loadFromFile(EcoPlugin.instance.worthyml)
        sender.msg("Reloaded worth.yml!")
        return true
    }
}