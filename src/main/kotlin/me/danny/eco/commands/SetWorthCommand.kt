package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.Item
import me.danny.eco.Worth
import me.danny.eco.commands.WorthCommand.allMats
import me.danny.eco.fmt
import me.danny.eco.humanize
import me.danny.eco.msg
import me.danny.eco.msgErr
import me.danny.eco.tryParseBigDec
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.math.BigDecimal

object SetWorthCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (!sender.hasPermission("dannyeco.admin")) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        if (args.isEmpty() || args.size != 3) {
            showHelp(sender, label)
            return true
        }

        val worth = EcoPlugin.instance.worth

        val material = Material.matchMaterial(args[1]!!)
        if (material == null) {
            sender.msgErr("Invalid item.")
            return true
        }

        var item = worth.get(material)
        if (item == null) {
            item = Item(material, BigDecimal("0.01"), 0)
            worth.items.add(item)
        }

        var arg = args[2]!!

        when (args[0]!!.lowercase()) {
            "limit" -> {
                val limit = arg.toIntOrNull()
                if (limit == null) {
                    sender.msgErr("Invalid limit. Must be 0 or more.")
                    return true
                }

                val old = item.limit
                item.limit = limit
                sender.msg("&7&o${humanize(item.material)}&e limit: &c&o$old -> &c$limit")
            }
            "value" -> {
                if (arg.startsWith("$")) arg = arg.substring(1)
                val value = tryParseBigDec(arg)
                if (value == null) {
                    sender.msgErr("Invalid value. Must be $0.00 or more.")
                    return true
                }

                val old = item.worth
                item.worth = value
                sender.msg("&7&o${humanize(item.material)}&e value: &c&o${old.fmt()} -> &c${value.fmt()}")
            }
            else -> {
                showHelp(sender, label)
                return true
            }
        }

        Worth.saveToFile(EcoPlugin.instance.worthyml, worth)
        EcoPlugin.instance.worth = Worth.loadFromFile(EcoPlugin.instance.worthyml)
        return true
    }

    private fun showHelp(s: CommandSender, label: String) {
        s.msgErr("This command lets you change the sell limit and value of items.")
        s.msgErr("To change the sell limit of an item: /$label limit <item> <limit>")
        s.msgErr("To change the value of an item: /$label value <item> <value>")
        s.msgErr("To remove a sell limit, set it to 0.")
        s.msgErr("Item value must be greater than $0.00.")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String?>? {
        if (!sender.hasPermission("dannyeco.admin")) return null
        if (args.size > 2) return null

        if (args.size <= 1) return listOf("limit", "value")
        return allMats.filter { id -> id.lowercase().startsWith(args[1]!!.lowercase()) }
    }
}