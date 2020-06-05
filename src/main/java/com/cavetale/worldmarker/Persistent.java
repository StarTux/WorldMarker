package com.cavetale.worldmarker;

public interface Persistent {
    /**
     * Override if desired.
     */
    default void onSave(MarkTagContainer container) { }

    /**
     * Override if desired.
     */
    default void onUnload(MarkTagContainer container) { }

    /**
     * Override if desired.
     */
    default void onTick(MarkTagContainer container) { }
}
