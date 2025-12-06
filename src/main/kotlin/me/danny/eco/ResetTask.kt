package me.danny.eco

import org.bukkit.Bukkit

object ResetTask : Runnable {
    override fun run() {
        Bukkit.getConsoleSender().msg("&d[DannyEco] &fTrade limits have been reset!")
        LimitTracking.resetAll()
    }
}