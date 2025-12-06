package me.danny.eco

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal
import kotlin.getValue
import net.milkbowl.vault.economy.Economy

private val econ by lazy(::getEconomy)

fun hasEconomy(): Boolean = Bukkit.getPluginManager().getPlugin("Vault") != null
private fun getEconomy(): Economy {
    if (!hasEconomy()) throw Exception("Attempted to get Vault instance with no Vault plugin active...")
    val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java)
        ?: throw Exception("Failed to load Economy from Vault...")
    return rsp.provider
}

internal fun giveMoney(player: Player, amount: BigDecimal) : Boolean {
    try {
        val resp = econ.depositPlayer(player, amount.toDouble())
        return resp.transactionSuccess()
    } catch (e: Exception) {
        EcoPlugin.instance.logger.warning("An error with Vault occurred. Do not panic! Player items were returned.")
        EcoPlugin.instance.logger.warning("This is the error:")
        e.printStackTrace()
        EcoPlugin.instance.logger.warning("Are you sure you have an economy plugin enabled? (EssentialsX)")
        return false
    }
}