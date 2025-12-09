package me.danny.eco

import org.bukkit.Bukkit

object ResetTask : Runnable {
    override fun run() {
        val config = EcoPlugin.instance.config

        if (config.announceLimitsReset) {
            Bukkit.getOnlinePlayers().forEach {
                pl -> pl.msg(config.announceMessage)
            }
            Bukkit.getConsoleSender().msg(config.announceMessage)
        } else if (config.tellConsoleLimitsReset) {
            Bukkit.getConsoleSender().msg(config.announceMessage)
        }

        EcoPlugin.instance.analytics.tick()
        LimitTracking.resetAll()
    }
}