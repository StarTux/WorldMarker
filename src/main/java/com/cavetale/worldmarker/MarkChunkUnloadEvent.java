package com.cavetale.worldmarker;

import lombok.NonNull;
import org.bukkit.event.HandlerList;

public final class MarkChunkUnloadEvent extends MarkChunkEvent {
    static final HandlerList HANDLERS = new HandlerList();

    MarkChunkUnloadEvent(@NonNull final MarkChunk chunk) {
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
