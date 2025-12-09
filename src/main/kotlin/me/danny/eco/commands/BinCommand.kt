package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.Graph
import me.danny.eco.msg
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

object BinCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (args.size != 1) return false

        val hist = EcoPlugin.instance.analytics.getHistory(args[0]!!)!!
        val data = hist.getFirstNDays(3)

        val graph = Graph.create(
            width = 60,
            height = 14,
            points = data
        )

//        if (sender is Player) {
//            val it = ItemStack(Material.BOOK, 1)
//            val im = it.itemMeta!!
//            im.lore = graph
//            im.setDisplayName("&e${args[0]} volume history".color())
//            it.itemMeta = im
//
//            sender.inventory.addItem(it)
//        }

        graph.forEach(sender::msg)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String?> = EcoPlugin.instance.analytics.allKeys().toList()
}