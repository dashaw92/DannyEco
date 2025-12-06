package me.danny.eco

import org.bukkit.Material
import org.bukkit.Tag

internal val cache: MutableMap<String, Tag<Material>?> = mutableMapOf()

internal fun getTag(name: String) : Tag<Material>? {
    val name = name.uppercase()

    //Attempt to reflect into Tag and find a matching Tag<Material>.
    //If the provided name doesn't exist in Tag.class, or the field isn't an instance of Tag<*>,
    //or we can prove the values aren't Materials (aka it's not a Tag<Material>), returns null.
    //Caches the result of this computation to minimize redundant reflection
    return cache.computeIfAbsent(name) {
        try {
            val field = Tag::class.java.getDeclaredField(it)
            val found = field.get(null)
            if (found !is Tag<*>) return@computeIfAbsent null
            if (found.values.isEmpty() || found.values.first() !is Material) return@computeIfAbsent null
            @Suppress("UNCHECKED_CAST")
            found as Tag<Material>
        } catch (_: Exception) {
            null
        }
    }
}