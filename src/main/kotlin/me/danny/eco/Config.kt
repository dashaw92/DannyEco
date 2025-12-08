package me.danny.eco

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.reflect.KProperty

private const val CURRENT_CONFIG_VERSION = 2

class Config(file: File) {
    companion object {
        private val file = EcoPlugin.instance.dataFolder.resolve("config.yml")

        private fun copyDefault() {
            EcoPlugin.instance.saveResource("config.yml", false)
        }

        fun loadFromFile() : Config {
            if (!file.exists()) copyDefault()

            val cfg = Config(file)
            if (cfg.loadedVersion < CURRENT_CONFIG_VERSION) {
                EcoPlugin.instance.logger.warning("Your config.yml is outdated.")
                EcoPlugin.instance.logger.warning("Rename your current config.yml to 'config.yml.old' to allow the plugin to generate a current config.")
                EcoPlugin.instance.logger.warning("If you do not do this, new options will be left at defaults, which may not be in your best interests.")
                EcoPlugin.instance.logger.warning("This message will be displayed on startup until this is resolved.")
            }

            return Config(file)
        }
    }

    private val yml = YamlConfiguration.loadConfiguration(file)

    private val loadedVersion: Int by ConfigProp(yml, $$"$version", 0)

    val resetDelayTicks: Long by ConfigProp(yml, "sell-limits.reset-delay-ticks", 20 * 60 * 60)
    val announceLimitsReset: Boolean by ConfigProp(yml, "sell-limits.announce-limits-reset", false)
    val tellConsoleLimitsReset: Boolean by ConfigProp(yml, "sell-limits.tell-console-limits-reset", true)
    val announceMessage: String by ConfigProp(yml, "sell-limits.announce-message", "&d[DannyEco] &7Sell limits have been refreshed!")

    val ecoAnalyticsEnabled: Boolean by ConfigProp(yml, "eco-analytics.enabled", true)
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