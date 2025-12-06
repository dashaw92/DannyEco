package me.danny.eco

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.reflect.KProperty

class Config(file: File) {

    companion object {
        private val file = EcoPlugin.instance.dataFolder.resolve("config.yml")

        private fun copyDefault() {
            EcoPlugin.instance.saveResource("config.yml", false)
        }

        fun loadFromFile() : Config {
            if (!file.exists()) copyDefault()
            return Config(file)
        }
    }

    private val yml = YamlConfiguration.loadConfiguration(file)

    var resetDelayTicks: Long by ConfigProp(yml, "sell-limits.reset-delay-ticks", 20 * 60 * 60)
    var announceLimitsReset: Boolean by ConfigProp(yml, "sell-limits.announce-limits-reset", false)
    var tellConsoleLimitsReset: Boolean by ConfigProp(yml, "sell-limits.tell-console-limits-reset", true)
    var announceMessage: String by ConfigProp(yml, "sell-limits.announce-message", "&d[DannyEco] &7Sell limits have been refreshed!")
}

class ConfigProp<T>(val yml: YamlConfiguration, val path: String, val fallback: T) {

    private var cached: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (cached != null) return cached ?: return fallback

        val found = yml.get(path, fallback)
        @Suppress("UNCHECKED_CAST")
        cached = found as T
        return cached!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        cached = null
        yml.set(path, value)
    }
}