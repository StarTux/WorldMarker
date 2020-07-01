package com.cavetale.worldmarker;

public interface Persistent extends Tickable {
    /**
     * Override if desired.
     */
    default void onSave(MarkTagContainer container) { }

    /**
     * Override if desired.
     */
    default void onUnload(MarkTagContainer container) { }
}
