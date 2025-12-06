package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.LimitTracking
import me.danny.eco.fmt
import me.danny.eco.humanize
import me.danny.eco.msg
import me.danny.eco.msgErr
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object WorthCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (!sender.hasPermission("dannyeco.sell")) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        if (!args.isEmpty()) {
            val material = Material.matchMaterial(args[0]!!)
            if (material == null) {
                sender.msgErr("Unknown material: &c${args[0]}")
                return true
            }

            showWorth(sender, material)
            return true
        }

        if (sender is Player) {
            val item = sender.inventory.itemInMainHand
            showWorth(sender, item.type)
            return true
        }

        sender.msgErr("Usage: /$label <item>")
        return true
    }

    private fun showWorth(s: CommandSender, m: Material) {
        val worth = EcoPlugin.instance.worth
        if (!worth.canBeSold(m)) {
            s.msgErr("Cannot sell this item to the server!")
            return
        }

        val item = worth.get(m)!!
        val limit = if (item.limit != 0 && s is Player) {
            val remaining = LimitTracking.remaining(s, m)!!
            " &cYour limit: ${item.limit - remaining}/${item.limit}&e."
        } else {
            ""
        }
        s.msg("&7&o${humanize(m)}&e: Worth &6${item.worth.fmt()}&e.${limit}")
    }

    internal val allMats = Material.entries.map { it.name }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String?>? {
        if (!sender.hasPermission("dannyeco.sell")) return null
        if (args.size > 1) return null

        if (args.isEmpty()) return allMats
        return allMats.filter { id -> id.lowercase().startsWith(args[0]!!.lowercase()) }
    }
}