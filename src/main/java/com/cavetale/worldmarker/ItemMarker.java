package com.cavetale.worldmarker;

import lombok.NonNull;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ItemMarker {
    static ItemMarker instance;
    final NamespacedKey key;

    ItemMarker(@NonNull final WorldMarkerPlugin plugin) {
        instance = this;
        key = new NamespacedKey(plugin, "id");
    }

    public static void setId(@NonNull ItemStack item, @NonNull String id) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.set(instance.key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    public static void resetId(@NonNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.remove(instance.key);
        item.setItemMeta(meta);
    }

    public static String getId(@NonNull ItemStack item) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        if (!tag.has(instance.key, PersistentDataType.STRING)) return null;
        return tag.get(instance.key, PersistentDataType.STRING);
    }
}
