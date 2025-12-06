package me.danny.eco

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.configuration.ConfigurationSection
import java.math.BigDecimal

sealed interface Item {
    val ymlPath: String
    var worth: BigDecimal
    var limit: Int

    fun materials(): Set<Material>

    fun name(): String
}

data class TagGroup(override val ymlPath: String, val name: String, val tag: Tag<Material>, override var worth: BigDecimal, override var limit: Int = 0) : Item {
    override fun materials(): Set<Material> = tag.values
    override fun name(): String = name
}
data class BasicItem(override val ymlPath: String, val material: Material, override var worth: BigDecimal, override var limit: Int = 0) : Item {
    override fun materials(): Set<Material> = setOf(material)
    override fun name(): String = humanize(material)
}

//Can return 0, 1, or many
//0 (empty) if error
//1 if the group refers to a concrete Material, e.g. Material.DIAMOND
//many if the group is the name of a Tag (group of materials- see org.bukkit.Tag)
internal fun parseItemsFromConfig(yml: ConfigurationSection, item: String) : Item? {
    val path = "worth.$item"
    val materials = getRelevantMaterials(item)

    if (materials.isEmpty()) {
        EcoPlugin.instance.logger.warning("[Worth] Invalid item or tag: $path. Skipping")
        return null
    }

    //worth.yml supports two formats:
    //The simple format, where the item only has a value and no sell limit:
    //<material>: <value>
    //The limited format, where the item has a value and sell limit:
    /*
    <material>:
      value: <value>
      limit: <limit>
     */
    val maybeWorth = yml.getString(path)

    val worth: BigDecimal?
    val limit: Int
    if (maybeWorth != null && yml.isDouble(path) || yml.isInt(path)) {
        val itemValue = tryParseBigDec(maybeWorth)
        worth = itemValue
        limit = 0
    } else {
        val itemValue = tryParseBigDec(yml.getString("$path.value"))
        val sellLimit = yml.getInt("$path.limit").coerceIn(0, Int.MAX_VALUE)


        worth = itemValue
        limit = sellLimit
    }

    if (worth == null || worth <= BigDecimal.ZERO) {
        EcoPlugin.instance.logger.warning("[Worth] For item $path: Item value must be greater than 0.")
        return null
    }

    return if (materials.size == 1) {
        BasicItem(path, materials.first(), worth, limit)
    } else {
        val tag = getTag(item)
        if (tag == null) {
            EcoPlugin.instance.logger.warning("[Worth] Invalid tag: $path. Skipping")
            return null
        }

        TagGroup(path, item, tag, worth, limit)
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