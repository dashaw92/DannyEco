package me.danny.eco.gui.views

import me.danny.eco.gui.ViewerInventory
import org.bukkit.inventory.ItemStack

abstract class View {
    abstract fun build(viewer: ViewerInventory)
    abstract fun onClick(viewer: ViewerInventory, item: ItemStack, slot: Int)
}
