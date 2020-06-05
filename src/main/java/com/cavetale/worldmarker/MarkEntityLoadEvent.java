package com.cavetale.worldmarker;

import lombok.NonNull;
import org.bukkit.event.HandlerList;

public final class MarkEntityLoadEvent extends MarkEntityEvent {
    static final HandlerList HANDLERS = new HandlerList();

    MarkEntityLoadEvent(@NonNull final MarkEntity entity) {
        super(entity);
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
