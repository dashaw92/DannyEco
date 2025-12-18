package me.danny.eco.gui

import me.danny.eco.color
import me.danny.eco.gui.views.PageView
import me.danny.eco.gui.views.View
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class ViewerInventory : InventoryHolder {
    private val viewer: Player
    private val inv: Inventory
    private var view: View

    constructor(viewer: Player) {
        viewers[viewer] = this

        this.viewer = viewer
        inv = Bukkit.createInventory(this, SIZE, "&d[DannyEco] &8Item Volume History".color())
        view = PageView()

        build()
        viewer.openInventory(inv)
    }

    private fun build() {
        inv.clear()
        view.build(this)
    }

    fun onClick(event: InventoryClickEvent) {
        view.onClick(this, event.getCurrentItem()!!, event.slot)
        build()
    }

    fun onClose() {
        viewers.remove(viewer)
    }

    fun setView(view: View) {
        this.view = view
    }

    fun getViewer(): Player {
        return viewer
    }

    override fun getInventory(): Inventory = inv

    companion object {
        private val viewers: MutableMap<Player, ViewerInventory> = mutableMapOf()
        private const val SIZE = 54

        fun onDisable() {
            viewers.keys.forEach(Player::closeInventory)
            viewers.clear()
        }

        fun refreshAll() {
            viewers.values.forEach(ViewerInventory::build)
        }
    }
}
