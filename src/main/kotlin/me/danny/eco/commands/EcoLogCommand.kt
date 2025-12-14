package me.danny.eco.commands

import me.danny.eco.*
import me.danny.eco.logging.SaleRecord
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime

object EcoLogCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission(Permissions.ECOLOG)) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        if (!EcoPlugin.instance.config.loggingEnabled) {
            sender.msgErr("Economy logging is not enabled.")
            return true
        }

        val logs = EcoPlugin.instance.ecolog.getLogs()
        if (logs.isEmpty()) {
            sender.msgErr("There are no sale logs to view.")
            return true
        }

        val linesPerPage = 15
        val pages = logs.size / linesPerPage
        val page = args.getOrNull(0)?.toIntOrNull()?.minus(1)?.coerceIn(0, pages) ?: 0

        sender.msg("Showing sale logs (${page + 1}/${pages + 1})")
        (page * linesPerPage until (page + 1) * linesPerPage)
            .filter { idx -> idx < logs.size }
            .map(logs::get)
            .map(::recordToComponent)
            .forEach(sender::sendMessage)

        return true
    }
}

private fun recordToComponent(sale: SaleRecord): Component {
    //TODO this becomes minecraft:items

    val elapsed = calcElapsed(sale.time)
    val player = Bukkit.getOfflinePlayer(sale.seller).name ?: "<???>"
    val base = Component.text().color(NamedTextColor.YELLOW)
        .append(
            Component.`object`(ObjectContents.sprite(Key.key("minecraft:item/clock_00"))).color(NamedTextColor.AQUA)
                .append(Component.text("$elapsed "))
        )
        .append(Component.`object`(ObjectContents.playerHead(sale.seller)).color(NamedTextColor.WHITE))
        .append(Component.text(player).color(NamedTextColor.BLUE))
        .append(Component.text(" sold "))
        .append(Component.text("${sale.amount} ").color(NamedTextColor.LIGHT_PURPLE))
        .append(Component.text(sale.item).color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
        .append(Component.text(" for "))
        .append(Component.text(sale.ext.fmt()).color(NamedTextColor.GOLD))
        .append(Component.text(" (").color(NamedTextColor.GREEN))
        .append(Component.text(sale.price.fmt()).color(NamedTextColor.DARK_GREEN))
        .append(Component.text(" ea.)").color(NamedTextColor.GREEN))
        .build()
    return base
}

private fun calcElapsed(time: ZonedDateTime): String {
    val duration = Duration.between(time, ZonedDateTime.now(ZoneOffset.UTC))
    val hours = duration.toHours()
    val mins = duration.toMinutes()
    val secs = duration.toSeconds()

    if (hours >= 1) return "${hours}h"
    if (mins >= 1) return "${mins}m"
    if (secs >= 1) return "${secs}s"
    return "Now"
}