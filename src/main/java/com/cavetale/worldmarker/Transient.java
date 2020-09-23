package com.cavetale.worldmarker;

import org.bukkit.plugin.java.JavaPlugin;

public interface Transient extends Tickable {
    JavaPlugin getPlugin();

    /**
     * Override if desired.
     */
    default void onUnload(MarkTagContainer container) { }
}
