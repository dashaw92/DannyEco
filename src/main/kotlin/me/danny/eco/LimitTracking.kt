package me.danny.eco

import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

internal object LimitTracking {
    private val M = mutableMapOf<UUID, MutableList<MaterialCount>>()

    private fun getCount(pl: Player, name: String) : MaterialCount? {
        val item = EcoPlugin.instance.worth.getItem(name)!!
        if (item.limit == 0) return null
        M.putIfAbsent(pl.uniqueId, mutableListOf())

        val counts = M[pl.uniqueId]!!
        val maybeCount = counts.find { mc -> mc.name == name }
        if (maybeCount == null) {
            val newCount = MaterialCount(name, item.limit)
            counts.add(newCount)
            return newCount
        }

        return maybeCount
    }

    internal fun remaining(pl: Player, name: String): Int? {
        return getCount(pl, name)?.count
    }

    internal fun add(pl: Player, name: String, amount: Int): Int? {
        val canSell = remaining(pl, name) ?: return null
        var willSell = amount
        if (canSell < amount) {
            willSell = canSell
        }

        getCount(pl, name)!!.count = canSell - willSell
        return willSell
    }

    internal fun resetAll() = M.clear()
}

private data class MaterialCount(val name: String, var count: Int)