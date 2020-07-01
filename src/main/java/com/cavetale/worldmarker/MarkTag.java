package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

/**
 * Custom data storage used for blocks, chunks, worlds, items,
 * entities.
 *
 * This class should not be exposed to the client plugin.  Its
 * properties and method should be encapsulated by public methods of
 * the containing class.
 */
final class MarkTag {
    String id;
    private Map<String, Object> data;
    // Initialized by WorldMarkerPlugin::onEnable
    static NamespacedKey idKey;
    static NamespacedKey dataKey;

    boolean hasData() {
        return data != null && !data.isEmpty();
    }

    Map<String, Object> getData() {
        if (data == null) data = new HashMap<>();
        return data;
    }

    void resetData() {
        data = null;
    }

    public boolean isEmpty() {
        return id == null && (data == null || data.isEmpty());
    }

    @Override
    public String toString() {
        return "id=" + id
            + " data=" + Json.serialize(data);
    }

    /**
     * Nullable.
     */
    static MarkTag load(PersistentDataHolder holder) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        if (container == null) return null;
        MarkTag markTag = new MarkTag();
        if (container.has(idKey, PersistentDataType.STRING)) {
            markTag.id = container.get(idKey, PersistentDataType.STRING);
        }
        if (container.has(dataKey, PersistentDataType.STRING)) {
            String json = container.get(dataKey, PersistentDataType.STRING);
            Map<Object, Object> map = (Map<Object, Object>) Json.deserialize(json, Map.class);
            if (map != null) {
                markTag.data = new HashMap<>();
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    if (!(entry.getKey() instanceof String)) continue;
                    markTag.data.put((String) entry.getKey(), entry.getValue());
                }
            }
        }
        return markTag;
    }

    /**
     * Save a MarkTag to NBT. If the MarkTag's id or data field is
     * null, they will be deleted in the target NBT tag. The point is
     * that they deserialize (this::load) to an identical MarkTag
     * object.
     */
    static void save(MarkTag markTag, PersistentDataHolder holder) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        if (container == null) return;
        if (markTag == null || markTag.id == null) {
            container.remove(idKey);
        } else {
            container.set(idKey, PersistentDataType.STRING, markTag.id);
        }
        if (markTag == null || markTag.data == null) {
            container.remove(dataKey);
        } else {
            container.set(dataKey, PersistentDataType.STRING, Json.serialize(markTag.data));
        }
    }
}
