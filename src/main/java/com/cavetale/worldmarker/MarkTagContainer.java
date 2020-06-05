package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;

abstract class MarkTagContainer {
    protected MarkTag tag; // This will be serialized to disk in some fashion
    private final Map<String, Persistent> persistentCache = new HashMap<>();
    private final Map<String, Transient> transientCache = new HashMap<>();

    MarkTag getTag() {
        if (tag == null) tag = new MarkTag();
        return tag;
    }

    public boolean hasTag() {
        return tag != null && !tag.isEmpty();
    }

    public boolean isEmpty() {
        return !hasTag() && persistentCache.isEmpty();
    }

    public final boolean hasId() {
        return tag != null && tag.id != null;
    }

    public final void setId(@NonNull final String id) {
        getTag().id = id;
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

    public boolean removePersistent(String key) {
        if (null == persistentCache.remove(key)) {
            return false;
        }
        removeRawData(key);
        return true;
    }

    public final <T extends Transient> T getTransient(String key, Class<T> type, Supplier<T> dfl) {
        Transient cached = transientCache.get(key);
        if (cached != null && type.isInstance(cached)) return type.cast(cached);
        T fresh = dfl.get();
        if (fresh != null) transientCache.put(key, fresh);
        return fresh;
    }

    public boolean removeTransient(String key) {
        return transientCache.remove(key) != null;
    }

    /**
     * Save at the next interval.
     */
    public abstract void save();

    /**
     * Every MarkBlock, Chunk and World will do this before saving to disk.
     */
    void prepareForSaving() {
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

    void onTick() {
        for (Map.Entry<String, Persistent> entry : persistentCache.entrySet()) {
            try {
                entry.getValue().onTick(this);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onTick: " + entry.getKey();
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
        for (Map.Entry<String, Transient> entry : transientCache.entrySet()) {
            try {
                entry.getValue().onTick(this);
            } catch (Exception e) {
                String msg = getClass().getSimpleName() + "::onTick: " + entry.getKey();
                WorldMarkerPlugin.instance.getLogger().log(Level.SEVERE, msg, e);
            }
        }
    }
}
