package me.danny.eco.commands

import me.danny.eco.*
import me.danny.eco.logging.EcoLogMgr
import me.danny.eco.logging.SaleRecord
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

object EcoLogCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission(Permissions.ECOLOG)) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        if (!EcoPlugin.instance.config.loggingEnabled) {
            sender.msgErr("Economy logging is not enabled.")
            return true
        }

        var pageArgIdx = 0
        val logs = if (args.isEmpty()) {
            EcoPlugin.instance.ecolog.getLogs()
        } else {
            val maybeLog = EcoLogMgr.loadHistorical(args[0])
            if (maybeLog == null) {
                EcoPlugin.instance.ecolog.getLogs()
            } else {
                pageArgIdx += 1
                maybeLog.records
            }
        }.sortedBy { it.time }.asReversed()

        if (logs.isEmpty()) {
            sender.msgErr("There are no sale logs to view.")
            return true
        }

        val linesPerPage = 15
        val pages = logs.size / linesPerPage
        val page = args.getOrNull(pageArgIdx)?.toIntOrNull()?.minus(1)?.coerceIn(0, pages) ?: 0

        sender.msg("Showing sale logs (${page + 1}/${pages + 1})")
        (page * linesPerPage until (page + 1) * linesPerPage)
            .filter { idx -> idx < logs.size }
            .map(logs::get)
            .map(partial(::recordToComponent, sender !is Player))
            .forEach(sender::sendMessage)

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        if (!sender.hasPermission(Permissions.ECOLOG)) return null
        if (args.size > 1) return null

        val available = EcoLogMgr.availableLogs().sorted()
        if (args.isEmpty()) return null
        return available.filter { it.lowercase().startsWith(args[0].lowercase()) }
    }
}

private fun recordToComponent(sale: SaleRecord, console: Boolean): Component {
    val base = Component.text().color(NamedTextColor.YELLOW)
        .append(calcElapsed(sale.time, console))
        .append(sellerInfo(sale.seller, console))
        .append(Component.text(": "))
        .append(Component.text("${sale.amount} ").color(NamedTextColor.LIGHT_PURPLE))
        .append(Component.text(sale.item).color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
        .append(Component.text(" "))
        .append(
            Component.text(sale.ext.fmt()).color(NamedTextColor.GOLD)
                .hoverEvent(
                    HoverEvent.showText(
                        Component.text("Each: ").color(NamedTextColor.GREEN)
                            .append(Component.text(sale.price.fmt()).color(NamedTextColor.DARK_GREEN))
                    )
                )
        )
        .build()
    return base
}

private fun calcElapsed(time: ZonedDateTime, console: Boolean): Component {
    val duration = Duration.between(time, ZonedDateTime.now(ZoneOffset.UTC))
    val days = duration.toDays()
    val hours = duration.toHours()
    val mins = duration.toMinutes()
    val secs = duration.toSeconds()

    val elapsed =
        if (days >= 1) "${days}d"
        else if (hours >= 1) "${hours}h"
        else if (mins >= 1) "${mins}m"
        else if (secs >= 1) "${secs}s"
        else "Now"

    return if (console) {
        Component.text()
    } else {
        Component.`object`(ObjectContents.sprite(Key.key("minecraft:item/clock_00")))
    }.color(NamedTextColor.DARK_GRAY)
        .asComponent()
        .append(Component.text("$elapsed "))
        .hoverEvent(HoverEvent.showText(
                Component.text(time.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)))
                    .color(NamedTextColor.GRAY)
                    .appendNewline()
                    .append(
                        Component.text(
                            time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                        ).color(NamedTextColor.DARK_GRAY)
                    )
            )
        )
}

private fun sellerInfo(uuid: UUID, console: Boolean): Component {
    val player = Bukkit.getOfflinePlayer(uuid).name ?: "<???>"

    return if (console) {
        Component.text()
    } else {
        Component.`object`(ObjectContents.playerHead(uuid))
    }.asComponent().color(NamedTextColor.WHITE)
        .append(Component.text(player).color(NamedTextColor.BLUE))
}

private fun <T, V, C> partial(f: (T, V) -> C, v: V): (T) -> C
//see what I did there? lol
        = { f(it, v) }