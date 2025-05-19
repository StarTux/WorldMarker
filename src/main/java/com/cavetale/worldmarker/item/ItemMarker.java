package com.cavetale.worldmarker.item;

import com.cavetale.worldmarker.WorldMarkerPlugin;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.util.Objects;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ItemMarker {
    private ItemMarker() { }

    public static void setId(@NonNull ItemStack item, @NonNull String id) {
        item.editPersistentDataContainer(tag -> {
                tag.set(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, id);
            });
    }

    public static void setId(@NonNull ItemMeta meta, @NonNull String id) {
        PersistentDataContainer tag = meta.getPersistentDataContainer();
        tag.set(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, id);
    }

    public static void resetId(@NonNull ItemStack item) {
        item.editPersistentDataContainer(tag -> {
                tag.remove(WorldMarkerPlugin.ID_KEY);
            });
    }

    public static String getId(@NonNull ItemStack item) {
        final PersistentDataContainerView tag = item.getPersistentDataContainer();
        if (tag == null) return null; // marked @NotNull but still
        return tag.getOrDefault(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, null);
    }

    public static boolean hasId(@NonNull ItemStack item) {
        return getId(item) != null;
    }

    public static boolean hasId(@NonNull ItemStack item, @NonNull String id) {
        return Objects.equals(getId(item), id);
    }
}
