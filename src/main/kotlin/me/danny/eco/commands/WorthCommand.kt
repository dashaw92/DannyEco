package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.Item
import me.danny.eco.LimitTracking
import me.danny.eco.fmt
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

        val worth = EcoPlugin.instance.worth
        if (!args.isEmpty()) {
            val item = worth.getItem(args[0]!!)
            showWorth(sender, item)
            return true
        }

        if (sender is Player) {
            val item = worth.getItem(sender.inventory.itemInMainHand.type.name)
            showWorth(sender, item)
            return true
        }

        sender.msgErr("Usage: /$label <item>")
        return true
    }

    private fun showWorth(s: CommandSender, item: Item?) {
        if (item == null) {
            s.msgErr("Cannot sell this item to the server!")
            return
        }

        val limit = if (item.limit != 0 && s is Player) {
            val remaining = LimitTracking.remaining(s, item.name())!!
            " &cYour limit: ${item.limit - remaining}/${item.limit}&e."
        } else {
            ""
        }
        s.msg("&7&o${item.name()}&e: Worth &6${item.worth.fmt()}&e.${limit}")
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