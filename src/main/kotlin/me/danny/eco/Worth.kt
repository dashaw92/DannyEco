package me.danny.eco

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class Worth(val items: MutableList<Item>) {
    companion object {
        private fun copyDefault() {
            EcoPlugin.instance.saveResource("worth.yml", false)
        }

        fun loadFromFile(file: File) : Worth {
            if (!file.exists()) copyDefault()
            return load(file)
        }

        private fun load(file: File) : Worth {
            val yml = YamlConfiguration.loadConfiguration(file)
            val items: MutableList<Item> = yml.getConfigurationSection("worth")!!
                .getKeys(false)
                .flatMap { parseItemsFromConfig(yml, it) } // and parse into Item instances
                .distinctBy(Item::material) //drop duplicates - Worth should behave like a Set.
                .toMutableList()
//                .onEach { EcoPlugin.instance.logger.info("- ${it.material} @ ${it.worth.fmt()} each (sell limit: ${it.limit})") }
            return Worth(items)
        }

        fun saveToFile(file: File, worth: Worth) {
            val yml = YamlConfiguration.loadConfiguration(file)

            for (item in worth.items.sortedBy { it.material.name }) {
                val path = "worth.${item.material.name.lowercase()}"
                if (item.limit == 0) {
                    yml.set(path, item.worth.toDouble())
                } else {
                    yml.set("$path.value", item.worth.toDouble())
                    yml.set("$path.limit", item.limit)
                }
            }

            try {
                yml.save(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun canBeSold(material: Material) : Boolean = items.map(Item::material).contains(material)
    fun get(needle: Material) : Item? = items.find { (material, _, _) -> needle == material }
}