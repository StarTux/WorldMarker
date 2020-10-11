package com.cavetale.worldmarker;

import lombok.NonNull;
import org.bukkit.event.HandlerList;

/**
 * This event is called for every non-empty loaded chunk, once per
 * tick.  Listen to this in order to update marked blocks once per
 * tick, while they're loaded.
 */
@Deprecated
public final class MarkChunkTickEvent extends MarkChunkEvent {
    static final HandlerList HANDLERS = new HandlerList();

    MarkChunkTickEvent(@NonNull final MarkChunk chunk) {
        super(chunk);
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
