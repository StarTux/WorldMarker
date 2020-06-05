package com.cavetale.worldmarker;

public interface Persistent {
    /**
     * Override if desired.
     */
    default void onSave() { }

    /**
     * Override if desired.
     */
    default void onUnload() { }

    /**
     * Override if desired.
     */
    default void onTick() { }
}
