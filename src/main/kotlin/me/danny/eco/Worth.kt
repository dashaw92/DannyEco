package me.danny.eco

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal

class Worth(val items: MutableList<Item>, val deleted: MutableSet<String>) {
    companion object {
        private fun copyDefault() {
            EcoPlugin.instance.saveResource("worth.yml", false)
        }

        fun loadFromFile(file: File): Worth {
            if (!file.exists()) copyDefault()
            return load(file)
        }

        private fun load(file: File): Worth {
            val yml = YamlConfiguration.loadConfiguration(file)
            val items: MutableList<Item> = yml.getConfigurationSection("worth")!!
                .getKeys(false)
                .mapNotNull { parseItemsFromConfig(yml, it) } // and parse into Item instances
                .distinctBy { it.ymlPath }
                .toMutableList()
            return Worth(items, mutableSetOf())
        }

        fun saveToFile(file: File, worth: Worth) {
            val yml = YamlConfiguration.loadConfiguration(file)

            for (item in worth.items.sortedBy { it.ymlPath }) {
                if (item.limit == 0) {
                    yml.set(item.ymlPath, item.worth.toDouble())
                } else {
                    yml.set("${item.ymlPath}.value", item.worth.toDouble())
                    yml.set("${item.ymlPath}.limit", item.limit)
                }
            }

            worth.deleted.forEach { yml.set(it, null) }

            try {
                yml.save(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun canBeSold(material: Material): Boolean = items.flatMap(Item::materials).contains(material)

    fun getItem(query: String): Item? {
        val mat = Material.matchMaterial(query) ?: return getTaggedItem(query)
        return get(mat)
    }

    fun getOrCreateItem(target: String): Item? {
        val existing = getItem(target)
        if (existing != null) return existing

        val mat = Material.matchMaterial(target)
        val item = if (mat == null) {
            val tag = getTag(target) ?: return null
            TagGroup("worth.${target.lowercase()}", target.lowercase(), tag, BigDecimal("0.01"), 0)
        } else {
            BasicItem("worth.${mat.name.lowercase()}", mat, BigDecimal("0.01"), 0)
        }

        items.add(item)
        return item
    }

    private fun get(needle: Material): Item? =
        items.find { it.materials().contains(needle) }

    private fun getTaggedItem(tag: String): TagGroup? = items.filterIsInstance<TagGroup>().find { it.name.equals(tag, true) }

    fun delete(item: Item) {
        items.removeIf { it.ymlPath == item.ymlPath }
        deleted.add(item.ymlPath)
    }
}