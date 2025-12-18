package me.danny.eco.gui.views

import me.danny.eco.EcoPlugin
import me.danny.eco.Graph
import me.danny.eco.Item
import me.danny.eco.color
import me.danny.eco.gui.ItemBuilder
import me.danny.eco.gui.ViewerInventory
import me.danny.eco.tracking.RESOLUTION
import me.danny.eco.tracking.UNITS
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class PageView : View() {
    private var page = 0
    override fun build(viewer: ViewerInventory) {
        val inv: Inventory = viewer.getInventory()
        val materials: List<Item> = EcoPlugin.instance.worth.items.sortedBy(Item::name)

        val limit: Int = inv.size - 9

        for (i in 0..<limit) {
            val index = page * limit + i
            if (index >= materials.size) break
            val item = materials[index]

            val hist = EcoPlugin.instance.analytics.getHistory(item.name())!!.getFirstNDays(7)
            val graph = Graph.create(points = hist, resolution = RESOLUTION, timescale = UNITS)
            val display = ItemBuilder.item(item.materials().first(), "&7&o${item.name()}".color(), graph)
            inv.setItem(i, display)
        }

        val pageMsg = "&ePage ${page + 1}/${materials.size / limit + 1}".color()

        inv.setItem(
            inv.size - 9,
            ItemBuilder.item(
                Material.RED_STAINED_GLASS_PANE,
                "&4<- Previous".color(),
                listOf(pageMsg)
            )
        )
        inv.setItem(
            inv.size - 8,
            ItemBuilder.item(
                Material.LIME_STAINED_GLASS_PANE,
                 "&2Next ->".color(),
                listOf(pageMsg)
            )
        )
    }

    override fun onClick(viewer: ViewerInventory, item: ItemStack, slot: Int) {
        val inv: Inventory = viewer.getInventory()
        var limit: Int = inv.size - 9
        if (limit == 0) limit = 1

        if (slot >= limit) {
            if (item.type == Material.RED_STAINED_GLASS_PANE && page > 0) page--
            if (item.type == Material.LIME_STAINED_GLASS_PANE && page < EcoPlugin.instance.worth.items.size / limit) page++
            return
        }
    }
}
