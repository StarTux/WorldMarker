package com.cavetale.worldmarker.util;

import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Some static helper functions to ease the typing effort.
 */
public final class Tags {
    private Tags() { }

    public static Byte getByte(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.BYTE) ? tag.get(key, PersistentDataType.BYTE) : null;
    }

    public static byte[] getByteArray(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.BYTE_ARRAY) ? tag.get(key, PersistentDataType.BYTE_ARRAY) : null;
    }

    public static Double getDouble(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.DOUBLE) ? tag.get(key, PersistentDataType.DOUBLE) : null;
    }

    public static Float getFloat(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.FLOAT) ? tag.get(key, PersistentDataType.FLOAT) : null;
    }

    public static Integer getInt(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.INTEGER) ? tag.get(key, PersistentDataType.INTEGER) : null;
    }

    public static int[] getIntArray(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.INTEGER_ARRAY) ? tag.get(key, PersistentDataType.INTEGER_ARRAY) : null;
    }

    public static Long getLong(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.LONG) ? tag.get(key, PersistentDataType.LONG) : null;
    }

    public static long[] getLongArray(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.LONG_ARRAY) ? tag.get(key, PersistentDataType.LONG_ARRAY) : null;
    }

    public static Short getShort(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.SHORT) ? tag.get(key, PersistentDataType.SHORT) : null;
    }

    public static String getString(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.STRING) ? tag.get(key, PersistentDataType.STRING) : null;
    }

    public static PersistentDataContainer getTag(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.TAG_CONTAINER) ? tag.get(key, PersistentDataType.TAG_CONTAINER) : null;
    }

    public static PersistentDataContainer[] getTagArray(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.TAG_CONTAINER_ARRAY) ? tag.get(key, PersistentDataType.TAG_CONTAINER_ARRAY) : null;
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, byte value) {
        tag.set(key, PersistentDataType.BYTE, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, byte[] value) {
        tag.set(key, PersistentDataType.BYTE_ARRAY, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, double value) {
        tag.set(key, PersistentDataType.DOUBLE, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, float value) {
        tag.set(key, PersistentDataType.FLOAT, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, int value) {
        tag.set(key, PersistentDataType.INTEGER, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, int[] value) {
        tag.set(key, PersistentDataType.INTEGER_ARRAY, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, long value) {
        tag.set(key, PersistentDataType.LONG, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, long[] value) {
        tag.set(key, PersistentDataType.LONG_ARRAY, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, short value) {
        tag.set(key, PersistentDataType.SHORT, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, String value) {
        tag.set(key, PersistentDataType.STRING, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, PersistentDataContainer value) {
        tag.set(key, PersistentDataType.TAG_CONTAINER, value);
    }

    public static void set(PersistentDataContainer tag, NamespacedKey key, PersistentDataContainer[] value) {
        tag.set(key, PersistentDataType.TAG_CONTAINER_ARRAY, value);
    }

    /**
     * Create a new tag and return it.
     * @param tag the parent tag
     * @return the new tag
     */
    public static PersistentDataContainer createTag(PersistentDataContainer tag) {
        return tag.getAdapterContext().newPersistentDataContainer();
    }

    /**
     * Create a new tag, overriding existing entries. This will always
     * save changes.
     */
    public static void createTag(PersistentDataContainer tag, NamespacedKey key, Consumer<PersistentDataContainer> callback) {
        PersistentDataContainer newTag = createTag(tag);
        callback.accept(newTag);
        set(tag, key, newTag);
    }

    /**
     * Get the named tag or create it if it doesn't exist.
     * @param tag the parent tag
     * @param key the key
     * @param callback The callback function. It receives the tag and
     * is to return true if changes should be saved, false otherwise.
     */
    public static void getOrCreateTag(PersistentDataContainer tag, NamespacedKey key, Function<PersistentDataContainer, Boolean> callback) {
        PersistentDataContainer newTag = tag.has(key, PersistentDataType.TAG_CONTAINER)
            ? tag.get(key, PersistentDataType.TAG_CONTAINER)
            : createTag(tag);
        if (callback.apply(newTag)) {
            set(tag, key, newTag);
        }
    }

    /**
     * Get the tag if it exists. The callback is never called with a
     * null parameter.
     * @param tag the parent tag
     * @param key the key
     * @param callback The callback function. It receives the tag if
     * it exists and is to return true if changes should to be saved,
     * false otherwise.
     * @return true if the tag exists, false otherwise.
     */
    public static boolean getTag(PersistentDataContainer tag, NamespacedKey key, Function<PersistentDataContainer, Boolean> callback) {
        if (!tag.has(key, PersistentDataType.TAG_CONTAINER)) return false;
        PersistentDataContainer newTag = tag.get(key, PersistentDataType.TAG_CONTAINER);
        if (callback.apply(newTag)) {
            tag.set(key, PersistentDataType.TAG_CONTAINER, newTag);
        }
        return true;
    }
}
