package me.danny.eco.gui

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

object GuiListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder !is ViewerInventory) return

        event.isCancelled = true
        val inv = event.inventory.holder as ViewerInventory?

        if (event.click.isKeyboardClick
            || event.isShiftClick
            || event.getCurrentItem() == null || event.getCurrentItem()!!
                .type == Material.AIR || (event.clickedInventory != inv!!.getInventory())
        ) {
            return
        }

        inv.onClick(event)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder !is ViewerInventory) return
        val inv = event.inventory.holder as ViewerInventory?
        inv!!.onClose()
    }
}
