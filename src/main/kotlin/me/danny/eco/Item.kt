package me.danny.eco

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import java.math.BigDecimal

data class Item(val material: Material, var worth: BigDecimal, var limit: Int = 0)

//Can return 0, 1, or many
//0 (empty) if error
//1 if the group refers to a concrete Material, e.g. Material.DIAMOND
//many if the group is the name of a Tag (group of materials- see org.bukkit.Tag)
internal fun parseItemsFromConfig(yml: ConfigurationSection, item: String) : List<Item> {
    val path = "worth.$item"
    val materials = getRelevantMaterials(item)
    //worth.yml supports two formats:
    //The simple format, where the item only has a value and no sell limit:
    //<material>: <value>
    //The limited format, where the item has a value and sell limit:
    /*
    <material>:
      value: <value>
      limit: <limit>
     */
    return materials.mapNotNull { material ->
        //check for the simple version first
        val maybeWorth = yml.getString(path)
        if (maybeWorth != null && yml.isDouble(path) || yml.isInt(path)) {
            val itemValue = tryParseBigDec(maybeWorth)
            if (itemValue == null) {
                EcoPlugin.instance.logger.warning("[Worth] Invalid entry for $path: $maybeWorth is not a valid number. Item will be skipped until this is fixed!")
                return@mapNotNull null
            }
            Item(material, itemValue, 0)
        } else {
            val itemValue = tryParseBigDec(yml.getString("$path.value"))
            val sellLimit = yml.getInt("$path.limit").coerceIn(0, Int.MAX_VALUE)

            if (itemValue == null || itemValue <= BigDecimal.ZERO) {
                EcoPlugin.instance.logger.warning("[Worth] For item $path: Item value must be greater than 0 (is currently \"${itemValue}\".")
                return@mapNotNull null
            }

            Item(material, itemValue, sellLimit)
        }
    }
}

//attempt to find matching material(s) from a name.
//if passed an actual item name, like "diamond", yields a singleton set of Material.DIAMOND.
//if passed an abstract name (matching a tagged set of materials), yields the whole set of materials in that Tag.
private fun getRelevantMaterials(name: String) : Set<Material> {
    val maybeMaterial = Material.matchMaterial(name)

    if (maybeMaterial == null) {
        val maybeTag = getTag(name) ?: return emptySet<Material>()
        return maybeTag.values
    }

    return setOf(maybeMaterial)
}

//DIAMOND_SWORD -> "Diamond Sword"
internal fun humanize(mat: Material) : String {
    return mat.name.split('_')
        .joinToString(separator = " ") {
            "${it[0].uppercase()}${it.substring(1).lowercase()}"
        }
}