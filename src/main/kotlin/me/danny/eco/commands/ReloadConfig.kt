package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.msg
import me.danny.eco.msgErr
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ReloadConfig : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (!sender.hasPermission("dannyeco.admin")) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        EcoPlugin.instance.configReload()
        sender.msg("Config reloaded!")
        return true
    }
}