package me.danny.eco

import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

internal object LimitTracking {
    private val M = mutableMapOf<UUID, MutableList<MaterialCount>>()

    private fun getCount(pl: Player, material: Material) : MaterialCount? {
        val item = EcoPlugin.instance.worth.get(material)!!
        if (item.limit == 0) return null
        M.putIfAbsent(pl.uniqueId, mutableListOf())

        val counts = M[pl.uniqueId]!!
        val maybeCount = counts.find { mc -> mc.material == material }
        if (maybeCount == null) {
            val newCount = MaterialCount(material, item.limit)
            counts.add(newCount)
            return newCount
        }

        return maybeCount
    }

    internal fun remaining(pl: Player, material: Material): Int? {
        return getCount(pl, material)?.count
    }

    internal fun add(pl: Player, material: Material, amount: Int): Int? {
        val canSell = remaining(pl, material) ?: return null
        var willSell = amount
        if (canSell < amount) {
            willSell = canSell
        }

        getCount(pl, material)!!.count = canSell - willSell
        return willSell
    }

    internal fun resetAll() = M.clear()
}

private data class MaterialCount(val material: Material, var count: Int)