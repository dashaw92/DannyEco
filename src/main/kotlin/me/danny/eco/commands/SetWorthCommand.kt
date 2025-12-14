package me.danny.eco.commands

import me.danny.eco.*
import me.danny.eco.commands.WorthCommand.allMats
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

object SetWorthCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission(Permissions.SET_WORTH)) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        if (args.size < 2) {
            showHelp(sender, label)
            return true
        }

        val worth = EcoPlugin.instance.worth
        val target = args[1].lowercase()
        val item = worth.getItem(target)

        when (args.size) {
            2 if args.first().lowercase() == "delete" -> {
                if (item == null) {
                    sender.msg("Invalid item or tag.")
                    return true
                }

                worth.delete(item)
                sender.msg("&7&o${item.name()} &7is off the market. Was &6${item.worth.fmt()} &7with a limit of &c${item.limit}&7.")
            }
            3 -> {
                val item = worth.getOrCreateItem(target)
                if (item == null) {
                    sender.msg("Invalid item or tag.")
                    return true
                }

                var arg = args[2]
                when (args.first().lowercase()) {
                    "limit" -> {
                        val limit = arg.toIntOrNull()
                        if (limit == null) {
                            sender.msgErr("Invalid limit. Must be 0 or more.")
                            return true
                        }

                        val old = item.limit
                        item.limit = limit
                        sender.msg("&7&o${item.name()}&e limit: &c&o$old -> &c$limit")
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
                        sender.msg("&7&o${item.name()}&e value: &c&o${old.fmt()} -> &c${value.fmt()}")
                    }

                    else -> {
                        showHelp(sender, label)
                        return true
                    }
                }
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
        s.msgErr("This command lets you change the sell limit, value of items, or make them unsellable.")
        s.msgErr("To change the sell limit of an item: /$label limit <item> <limit>")
        s.msgErr("To change the value of an item: /$label value <item> <value>")
        s.msgErr("&cTo make an item unsellable: /$label delete <item>")
        s.msgErr("To remove a sell limit, set it to 0.")
        s.msgErr("Item value must be greater than $0.00.")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String?>? {
        if (!sender.hasPermission("dannyeco.admin")) return null
        if (args.size > 2) return null

        if (args.size <= 1) return listOf("limit", "value", "delete")
        return allMats.filter { id -> id.lowercase().startsWith(args[1].lowercase()) }
    }
}