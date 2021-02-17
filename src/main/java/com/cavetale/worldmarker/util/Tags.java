package com.cavetale.worldmarker.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class Tags {
    private Tags() { }

    public static void set(PersistentDataContainer tag, NamespacedKey key, String value) {
        tag.set(key, PersistentDataType.STRING, value);
    }

    public static String getString(PersistentDataContainer tag, NamespacedKey key) {
        return tag.has(key, PersistentDataType.STRING) ? tag.get(key, PersistentDataType.STRING) : null;
    }
}
