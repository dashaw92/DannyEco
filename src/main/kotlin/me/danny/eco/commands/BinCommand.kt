package me.danny.eco.commands

import me.danny.eco.Permissions
import me.danny.eco.gui.ViewerInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object BinCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission(Permissions.BIN)) return true
        if (sender !is Player) return true
        ViewerInventory(sender)
//        if (args.size != 1) return false
//
//        val hist = EcoPlugin.instance.analytics.getHistory(args[0])!!
//        val data = hist.getFirstNDays(30)
//
//        val graph = Graph.create(
//            timescale = "hours",
//            points = data
//        )
//
//        if (sender is Player) {
//            val it = ItemStack(Material.BOOK, 1)
//            val im = it.itemMeta!!
//            im.lore = graph
//            im.setDisplayName("&e${args[0]} volume history".color())
//            it.itemMeta = im
//
//            sender.inventory.addItem(it)
//        }
//
//        graph.forEach(sender::msg)
        return true
    }

//    override fun onTabComplete(
//        sender: CommandSender,
//        command: Command,
//        label: String,
//        args: Array<String>
//    ): List<String> = EcoPlugin.instance.analytics.allKeys().toList()
}