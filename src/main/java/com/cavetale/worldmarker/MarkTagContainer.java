package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;

/**
 * Superclass of MarkBlock, MarkChunk and MarkWorld.
 * Maybe more in the future.
 */
public abstract class MarkTagContainer {
    protected MarkTag tag; // This will be serialized to disk in some fashion
    private final Map<String, Persistent> persistentCache = new HashMap<>();
    private final Map<String, Transient> transientCache = new HashMap<>();

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

    public final void removeRawData(String key) {
        if (tag == null) return;
        if (!tag.hasData()) return;
        tag.getData().remove(key);
    }

    public final void resetRawData() {
        if (tag == null) return;
        tag.resetData();
    }

    public final void reset() {
        tag = null;
        persistentCache.clear();
        transientCache.clear();
    }

    public final <T extends Persistent> T getPersistent(String key, Class<T> type, Supplier<T> dfl) {
        // Either stored in cache...
        Persistent cached = persistentCache.get(key);
        if (cached != null && type.isInstance(cached)) return type.cast(cached);
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
        T fresh = dfl.get();
        if (fresh != null) persistentCache.put(key, fresh);
        return fresh;
    }

    public final boolean removePersistent(String key) {
        Persistent persistent = persistentCache.remove(key);
        if (persistent == null) {
            return false;
        }
        removeRawData(key);
        persistent.onUnload(this);
        return true;
    }

    public final <T extends Transient> T getTransient(String key, Class<T> type, Supplier<T> dfl) {
        Transient cached = transientCache.get(key);
        if (cached != null && type.isInstance(cached)) return type.cast(cached);
        T fresh = dfl.get();
        if (fresh != null) transientCache.put(key, fresh);
        return fresh;
    }

    public final boolean removeTransient(String key) {
        return transientCache.remove(key) != null;
    }

    /**
     * Save at the next interval.
     */
    public abstract void save();

    /**
     * Every MarkBlock, Chunk and World will do this before saving to disk.
     */
    public void prepareForSaving() {
        for (Map.Entry<String, Persistent> entry : persistentCache.entrySet()) {
            String key = entry.getKey();
            Persistent value = entry.getValue();
            String json;
            try {
                value.onSave(this);
                json = Json.serialize(value);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::prepareForSaving: " + key;
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
                continue;
            }
            setRawData(key, json);
        }
    }

    /**
     * Every MarkBlock and Chunk will do this before unloading.
     * Prepare for saving and clear presistent and transient data.
     */
    void onUnload() {
        for (Map.Entry<String, Persistent> entry : persistentCache.entrySet()) {
            try {
                entry.getValue().onUnload(this);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onUnload: " + entry.getKey();
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
        for (Map.Entry<String, Transient> entry : transientCache.entrySet()) {
            try {
                entry.getValue().onUnload(this);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onUnload: " + entry.getKey();
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
        prepareForSaving();
        persistentCache.clear();
        transientCache.clear();
    }

    abstract protected void tickTickable(Tickable t);

    /**
     * Implementers should call super::onTick().
     */
    void onTick() {
        for (Map.Entry<String, Persistent> entry : persistentCache.entrySet()) {
            try {
                tickTickable(entry.getValue());
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onTick: " + entry.getKey();
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
        for (Map.Entry<String, Transient> entry : transientCache.entrySet()) {
            try {
                tickTickable(entry.getValue());
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onTick: " + entry.getKey();
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
    }
}
