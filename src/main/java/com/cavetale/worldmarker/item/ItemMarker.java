package com.cavetale.worldmarker.item;

import com.cavetale.worldmarker.WorldMarkerPlugin;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ItemMarker {
    private ItemMarker() { }

    public static void setId(@NonNull ItemStack item, @NonNull String id) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.set(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    public static void setId(@NonNull ItemMeta meta, @NonNull String id) {
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.set(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, id);
    }

    public static void resetId(@NonNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.remove(WorldMarkerPlugin.ID_KEY);
        item.setItemMeta(meta);
    }

    public static String getId(@NonNull ItemStack item) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        if (!tag.has(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING)) return null;
        return tag.get(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING);
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
