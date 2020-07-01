package com.cavetale.worldmarker;

public interface Transient extends Tickable {
    /**
     * Override if desired.
     */
    default void onUnload(MarkTagContainer container) { }
}
