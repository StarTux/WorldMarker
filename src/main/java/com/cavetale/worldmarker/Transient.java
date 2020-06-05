package com.cavetale.worldmarker;

public interface Transient {
    /**
     * Override if desired.
     */
    default void onUnload() { }

    /**
     * Override if desired.
     */
    default void onTick() { }
}
