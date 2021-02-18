package com.cavetale.worldmarker.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public static final PersistentDataType[] ALL_DATA_TYPES = {
        PersistentDataType.BYTE,
        PersistentDataType.BYTE_ARRAY,
        PersistentDataType.DOUBLE,
        PersistentDataType.FLOAT,
        PersistentDataType.INTEGER,
        PersistentDataType.INTEGER_ARRAY,
        PersistentDataType.LONG,
        PersistentDataType.LONG_ARRAY,
        PersistentDataType.SHORT,
        PersistentDataType.STRING,
        PersistentDataType.TAG_CONTAINER,
        PersistentDataType.TAG_CONTAINER_ARRAY
    };

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

    public static Object get(PersistentDataContainer tag, NamespacedKey key) {
        for (PersistentDataType type : ALL_DATA_TYPES) {
            if (tag.has(key, type)) return tag.get(key, type);
        }
        return null;
    }

    /**
     * Turn a generic object from a PersistentDataContainer into a
     * Java Object. Primitive types will stay primitive. Containers
     * will become maps. Arrays will become Lists.
     * Works recursively!
     */
    public static Object toJavaObject(Object o) {
        if (o == null) return null;
        if (o instanceof PersistentDataContainer) {
            return toMap((PersistentDataContainer) o);
        } else if (o instanceof Object[]) {
            Object[] array = (Object[]) o;
            List<Object> list = new ArrayList<>(array.length);
            for (Object p : array) {
                list.add(toJavaObject(p));
            }
            return list;
        } else if (o instanceof byte[]) {
            return Arrays.asList((byte[]) o);
        } else if (o instanceof int[]) {
            return Arrays.asList((int[]) o);
        } else if (o instanceof long[]) {
            return Arrays.asList((long[]) o);
        } else if (o instanceof Number) {
            return (Number) o;
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return (String) o;
        } else {
            return o.toString();
        }
    }

    /**
     * Works recursively!
     */
    public static Map<NamespacedKey, Object> toMap(PersistentDataContainer tag) {
        Map map = new LinkedHashMap<>();
        for (NamespacedKey key : tag.getKeys()) {
            Object o = get(tag, key);
            Object p = toJavaObject(o);
            if (p == null) continue;
            map.put(key, p);
        }
        return map;
    }
}
