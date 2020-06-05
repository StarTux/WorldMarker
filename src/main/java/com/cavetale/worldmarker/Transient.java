package com.cavetale.worldmarker;

public interface Transient {
    /**
     * Override if desired.
     */
    default void onUnload(MarkTagContainer container) { }

    /**
     * Override if desired.
     */
    default void onTick(MarkTagContainer container) { }
}
