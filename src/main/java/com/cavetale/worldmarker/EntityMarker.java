package com.cavetale.worldmarker;

import lombok.NonNull;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class EntityMarker {
    static EntityMarker instance;
    final NamespacedKey key;

    EntityMarker(@NonNull final WorldMarkerPlugin plugin) {
        instance = this;
        key = new NamespacedKey(plugin, "id");
    }

    public static void setId(@NonNull Entity entity, @NonNull String id) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        tag.set(instance.key, PersistentDataType.STRING, id);
    }

    public static void resetId(@NonNull Entity entity) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        tag.remove(instance.key);
    }

    public static String getId(@NonNull Entity entity) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        if (!tag.has(instance.key, PersistentDataType.STRING)) return null;
        return tag.get(instance.key, PersistentDataType.STRING);
    }

    public static boolean hasId(@NonNull Entity entity, @NonNull String id) {
        PersistentDataContainer tag = entity.getPersistentDataContainer();
        if (!tag.has(instance.key, PersistentDataType.STRING)) return false;
        return id.equals(tag.get(instance.key, PersistentDataType.STRING));
    }
}
