package com.cavetale.worldmarker;

import org.bukkit.plugin.java.JavaPlugin;

public interface Persistent extends Tickable {
    JavaPlugin getPlugin();

    default boolean shouldSave() {
        return true;
    }

    /**
     * Override if desired.
     */
    default void onSave(MarkTagContainer container) { }

    /**
     * Override if desired.
     */
    default void onUnload(MarkTagContainer container) { }
}
