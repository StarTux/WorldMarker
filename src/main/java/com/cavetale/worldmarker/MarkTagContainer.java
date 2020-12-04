package com.cavetale.worldmarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Superclass of MarkBlock, MarkChunk and MarkWorld.
 * Maybe more in the future.
 */
public abstract class MarkTagContainer {
    protected MarkTag tag; // This will be serialized to disk in some fashion
    private final Map<String, Persistent> persistentCache = new HashMap<>();

    final MarkTag getTag() {
        if (tag == null) tag = new MarkTag();
        return tag;
    }

    public final boolean hasTag() {
        return tag != null && !tag.isEmpty();
    }

    /**
     * Return true if and only if this object is empty and therefore
     * can safely be removed and skipped when writing to disk.
     */
    public boolean isEmpty() {
        return !hasTag() && persistentCache.isEmpty();
    }

    public final boolean hasId() {
        return tag != null && tag.id != null;
    }

    public final void setId(@NonNull final String id) {
        getTag();
        if (tag.id != null) {
            removePersistent(tag.id); // Convention
            tag.id = null;
        }
        tag.id = id;
    }

    public final void resetId() {
        if (tag == null) return;
        tag.id = null;
        removePersistent(tag.id); // Convention
    }

    public final String getId() {
        if (tag == null) return null;
        return tag.id;
    }

    public final boolean hasId(@NonNull String id) {
        if (tag == null) return false;
        return id.equals(tag.id);
    }

    public final Object getRawData(String key) {
        if (tag == null || !tag.hasData()) return null;
        return tag.getData().get(key);
    }

    public final void setRawData(String key, Object value) {
        if (tag == null) tag = new MarkTag();
        tag.getData().put(key, value);
    }

    public final boolean removeRawData(String key) {
        if (tag == null) return false;
        if (!tag.hasData()) return false;
        return null != tag.getData().remove(key);
    }

    public final void resetRawData() {
        if (tag == null) return;
        tag.resetData();
    }

    public final void reset() {
        tag = null;
        persistentCache.clear();
    }

    /**
     * Get the persistent data or deserialize it, or create it.
     */
    public final <T extends Persistent> T getPersistent(JavaPlugin javaPlugin, String key, Class<T> type, Supplier<T> dfl) {
        // Either stored in cache...
        Persistent cached = persistentCache.get(key);
        if (cached != null && cached.getPlugin() == javaPlugin && type.isInstance(cached)) return type.cast(cached);
        // ...or stored in raw data...
        Object raw = getRawData(key);
        if (raw != null && raw instanceof String) {
            String json = (String) raw;
            try {
                T stored = Json.deserialize(json, type);
                persistentCache.put(key, stored);
                return stored;
            } catch (Exception e) {
                Logger logger = WorldMarkerPlugin.instance.getLogger();
                logger.log(Level.SEVERE, "Raw: " + json);
                logger.log(Level.SEVERE, "MarkTagContainer::getPersistent", e);
            }
        }
        // ...or all new all fresh...
        if (dfl == null) return null;
        T fresh = dfl.get();
        if (fresh != null) persistentCache.put(key, fresh);
        return fresh;
    }

    /**
     * Get the persistent data, only if it exists.
     */
    public final <T extends Persistent> T getPersistent(String key, Class<T> type) {
        Persistent cached = persistentCache.get(key);
        if (cached != null && type.isInstance(cached)) return type.cast(cached);
        return null;
    }

    /**
     * Remove persistent, along with its backing storage.
     */
    public final boolean removePersistent(String key) {
        Persistent persistent = persistentCache.remove(key);
        if (persistent != null) {
            persistent.onUnload(this);
        }
        boolean wasRaw = removeRawData(key);
        return wasRaw || persistent != null;
    }

    /**
     * Save at the next interval.
     */
    public abstract void save();

    /**
     * Every MarkBlock, Chunk and World will do this before saving to disk.
     */
    public void prepareForSaving() {
        for (String key : getPersistentKeys()) {
            Persistent persistent = persistentCache.get(key);
            if (persistent.shouldSave()) {
                try {
                    persistent.onSave(this);
                    setRawData(key, Json.serialize(persistent));
                } catch (Exception e) {
                    String msg = getClass().getSimpleName() + "::prepareForSaving: " + key;
                    WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
                    continue;
                }
            } else {
                removeRawData(key);
            }
        }
    }

    /**
     * Every MarkBlock and Chunk will do this before unloading.
     * Prepare for saving and clear presistent and transient data.
     */
    void onUnload() {
        for (String key : getPersistentKeys()) {
            Persistent persistent = persistentCache.get(key);
            try {
                persistent.onUnload(this);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onUnload: " + key;
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
        prepareForSaving();
        persistentCache.clear();
    }

    protected abstract void tickTickable(Tickable t);

    /**
     * Implementers should call super::onTick().
     */
    void onTick() {
        for (String key : getPersistentKeys()) {
            Persistent persistent = persistentCache.get(key);
            try {
                tickTickable(persistent);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onTick: " + key;
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
    }

    public final List<String> getPersistentKeys() {
        return new ArrayList<>(persistentCache.keySet());
    }

    final void removePlugin(JavaPlugin plugin) {
        boolean didRemove = false;
        for (String key : getPersistentKeys()) {
            Persistent persistent = persistentCache.get(key);
            if (persistent.getPlugin() != plugin) continue;
            try {
                persistent.onUnload(this);
                if (persistent.shouldSave()) {
                    persistent.onSave(this);
                    setRawData(key, Json.serialize(persistent));
                } else {
                    removeRawData(key);
                }
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::removePlugin: " + key;
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
            persistentCache.remove(key);
            didRemove = true;
        }
        if (didRemove) save();
    }
}
