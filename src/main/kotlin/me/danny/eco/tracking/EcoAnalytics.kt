package me.danny.eco.tracking

import me.danny.eco.EcoPlugin
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object EcoAnalytics {

    private val file = EcoPlugin.instance.dataFolder.resolve("item-history.bin")
    internal val history: MutableMap<String, ItemVolumeHistory> = mutableMapOf()

    fun load() {
        if (!file.exists()) file.createNewFile()
        try {
            val read = ObjectInputStream(GZIPInputStream(FileInputStream(file))).use(ObjectInputStream::readObject)
            if (read is Map<*, *>) {

                for ((k, v) in read) {
                    if (k !is String || v !is ItemVolumeHistory) continue
                    v.bins[0] = 512000
                    history[k] = v
                }
            }
        } catch (_: Exception) {}

        for (item in EcoPlugin.instance.worth.items) {
            history.putIfAbsent(item.name(), ItemVolumeHistory())
        }
    }

    fun save() {
        if (!file.exists()) file.createNewFile()
        ObjectOutputStream(GZIPOutputStream(FileOutputStream(file))).use { obj -> obj.writeObject(history) }
    }
}