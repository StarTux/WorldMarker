package com.cavetale.worldmarker.entity;

import com.cavetale.worldmarker.WorldMarkerPlugin;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class EntityMarker {
    private EntityMarker() { }

    public static void setId(@NonNull Entity entity, @NonNull String id) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        tag.set(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, id);
    }

    public static void resetId(@NonNull Entity entity) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        tag.remove(WorldMarkerPlugin.ID_KEY);
    }

    public static String getId(@NonNull Entity entity) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        if (!tag.has(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING)) return null;
        return tag.get(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING);
    }

    public static boolean hasId(@NonNull Entity entity) {
        String itemId = getId(entity);
        return itemId != null;
    }

    public static boolean hasId(@NonNull Entity entity, @NonNull String id) {
        String itemId = getId(entity);
        return itemId != null && itemId.equals(id);
    }
}
