package me.danny.eco.commands

import me.danny.eco.EcoPlugin
import me.danny.eco.LimitTracking
import me.danny.eco.color
import me.danny.eco.fmt
import me.danny.eco.giveMoney
import me.danny.eco.humanize
import me.danny.eco.msg
import me.danny.eco.msgErr
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

object SellCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>): Boolean {
        if (sender !is Player) {
            sender.msgErr("Cannot use this command from console.")
            return true
        }

        if (!sender.hasPermission("dannyeco.sell")) {
            sender.msgErr("You lack permission for this command.")
            return true
        }

        if (args.isEmpty()) {
            showHelp(sender, label)
            return true
        }

        when (args[0]!!.lowercase()) {
            "hand" -> sellHand(sender, args)
            "inventory" -> sellInventory(sender) { true }
            "blocks" -> sellInventory(sender) { it.type.isBlock }
            "items" -> sellInventory(sender) { !it.type.isBlock }
            else -> showHelp(sender, label)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String?>? {
        if (!sender.hasPermission("dannyeco.sell")) return null
        if (args.size > 1) return null

        val allIds = listOf("hand", "inventory", "blocks", "items")
        if (args.isEmpty()) return allIds
        return allIds.filter { id -> id.lowercase().startsWith(args[0]!!.lowercase()) }
    }

    private val SLOTS = run {
        val baseInv = (0 .. 35).toMutableList()
        baseInv.add(40) //off-hand slot
        baseInv
    }

    private fun sellHand(pl: Player, args: Array<out String?>) {
        val worth = EcoPlugin.instance.worth
        val item = pl.inventory.itemInMainHand.type
        if (item.isAir || !worth.canBeSold(item)) {
            pl.msgErr("Cannot sell this item to the server!")
            return
        }

        var amount = args.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1)
        val limit = LimitTracking.remaining(pl, item)
        if (limit != null) {
            if (limit == 0) {
                pl.msgErr("You have hit the daily limit for &o${humanize(item)}&7.")
                return
            }

            if (amount == null || amount > limit) {
                amount = limit
            }
        }

        var sold = 0
        for (i in SLOTS) {
            val it = pl.inventory.getItem(i)
            if (it == null || it.type != item) continue

            val stackSize = it.amount
            if (amount == null) {
                pl.inventory.setItem(i, null)
                sold += stackSize
            } else {
                if (amount >= stackSize) {
                    pl.inventory.setItem(i, null)
                    amount -= stackSize
                    sold += stackSize
                } else {
                    it.amount = stackSize - amount
                    sold += amount
                    break
                }
            }
        }

        LimitTracking.add(pl, item, sold)
        trySell(pl, item, sold)
    }

    private fun sellInventory(pl: Player, filter: (ItemStack) -> Boolean) {
        val seen = mutableSetOf<Material>()

        var soldAnything = false
        for (i in SLOTS) {
            val it = pl.inventory.getItem(i)
            if (it == null || !EcoPlugin.instance.worth.canBeSold(it.type) || !filter(it)) continue

            var amount = it.amount
            val limit = LimitTracking.remaining(pl, it.type)
            if (limit != null) {
                if (limit == 0) {
                    if (!seen.contains(it.type)) {
                        pl.msgErr("You have hit the daily limit for &o${humanize(it.type)}&7.")
                        seen.add(it.type)
                    }
                    continue
                }

                if (amount > limit) amount = limit
                LimitTracking.add(pl, it.type, amount)
            }

            if (amount == it.amount) {
                pl.inventory.setItem(i, null)
            } else {
                it.amount = amount
            }

            trySell(pl, it.type, amount)
            soldAnything = true
        }

        if (!soldAnything) {
            pl.msgErr("You have nothing to sell!")
        }
    }

    private fun showHelp(sender: CommandSender, label: String) {
        """
            &f[&dDannyEco&7 - &7sell&f]
            &7This command allows you to sell your items for money.
            &7You must provide one of the following options:
            &9/$label &3hand &a[amount]&e - Sells up to &a[amount]&e of held item.
            &eIf &a[amount]&e is not specified, &aall&e will be sold.
            &9/$label &3inventory &e- Sells &aall&e items that can be sold.
            &9/$label &3blocks &e- Sells &aall&e sellable blocks.
            &9/$label &3items &e- Sells &aall&e sellable items.
        """.trimIndent()
            .lines()
            .map(String::color)
            .forEach(sender::sendMessage)
    }
}

private fun trySell(pl: Player, it: Material, amount: Int) {
    val profit = EcoPlugin.instance.worth.get(it)!!.worth * BigDecimal(amount)
    if (!giveMoney(pl, profit)) {
        pl.msgErr("An error occurred trying to sell your items. Your items have been returned.")
        pl.inventory.addItem(ItemStack(it, amount))
    } else {
        pl.msg("&eSold &d${amount} &7&o${humanize(it)} &efor &6${profit.fmt()}&e.")
    }
}