package com.cavetale.worldmarker;

import java.util.Collection;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called for every non-empty loaded chunk, once per
 * tick.  Listen to this in order to update marked blocks once per
 * tick, while they're loaded.
 */
public final class MarkChunkTickEvent extends Event {
    static final HandlerList HANDLERS = new HandlerList();
    final MarkChunk markChunk;

    MarkChunkTickEvent(@NonNull final MarkChunk markChunk) {
        this.markChunk = markChunk;
    }

    public MarkChunk getChunk() {
        return markChunk;
    }

    public Collection<MarkBlock> getBlocks() {
        return markChunk.getBlocks();
    }

    public Stream<MarkBlock> getBlocksWithId(@NonNull String id) {
        return markChunk.getBlocks().stream()
            .filter(b -> b.hasId(id));
    }

    // Event Protocol

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
