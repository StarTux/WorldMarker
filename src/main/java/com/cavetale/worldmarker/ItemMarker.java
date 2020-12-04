package com.cavetale.worldmarker;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public final class ItemMarker {
    final WorldMarkerPlugin plugin;
    static ItemMarker instance;

    public ItemMarker enable() {
        instance = this;
        return this;
    }

    public MarkItem getItem(@NonNull ItemStack item) {
        return new MarkItem(plugin, item);
    }

    public static void setId(@NonNull ItemStack item, @NonNull String id) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.set(MarkTag.idKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    public static void resetId(@NonNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.remove(MarkTag.idKey);
        item.setItemMeta(meta);
    }

    public static String getId(@NonNull ItemStack item) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        if (!tag.has(MarkTag.idKey, PersistentDataType.STRING)) return null;
        return tag.get(MarkTag.idKey, PersistentDataType.STRING);
    }

    public static boolean hasId(@NonNull ItemStack item) {
        String itemId = getId(item);
        return itemId != null;
    }

    public static boolean hasId(@NonNull ItemStack item, @NonNull String id) {
        String itemId = getId(item);
        return itemId != null && itemId.equals(id);
    }
}
