package me.danny.eco.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

object ItemBuilder {

    fun item(material: Material, name: String, lore: List<String>): ItemStack {
        val `is` = ItemStack(material, 1)
        val im = `is`.itemMeta
        im.setDisplayName(name)
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        im.lore = lore
        `is`.setItemMeta(im)
        return `is`
    }
}
