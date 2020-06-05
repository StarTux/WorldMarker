package com.cavetale.worldmarker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;

/**
 * Superclass of MarkEntity related events.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MarkEntityEvent extends Event {
    @Getter final MarkEntity entity;
}
