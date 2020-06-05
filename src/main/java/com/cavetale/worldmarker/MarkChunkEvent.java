package com.cavetale.worldmarker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;

/**
 * Superclass of MarkChunk related events.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MarkChunkEvent extends Event {
    @Getter final MarkChunk chunk;
}
