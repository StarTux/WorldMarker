package com.cavetale.worldmarker;

/**
 * Super-interface of Persistent.
 */
public interface Tickable {
    /**
     * Override if desired.
     */
    default void onTickMarkBlock(MarkBlock markBlock) {
        onTick(markBlock);
    }

    /**
     * Override if desired.
     */
    default void onTickMarkChunk(MarkChunk markChunk) {
        onTick(markChunk);
    }

    /**
     * Override if desired.
     */
    default void onTickMarkWorld(MarkWorld markWorld) {
        onTick(markWorld);
    }

    /**
     * Override if desired.
     */
    default void onTickMarkEntity(MarkEntity markEntity) {
        onTick(markEntity);
    }

    /**
     * Override if desired.
     */
    default void onTick(MarkTagContainer container) {
    }
}
