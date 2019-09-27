package com.cavetale.worldmarker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Call every time a player clicks an item marked with an ID in their
 * hand or off-hand.
 * Cancelling this event will also cancel the causing event.
 */
@RequiredArgsConstructor @Getter
public final class MarkedItemUseEvent extends Event implements Cancellable {
    static final HandlerList HANDLERS = new HandlerList();
    final Player player;
    final ItemStack item;
    final String id;
    final EquipmentSlot hand;
    final ClickType click;
    final Entity entity;
    final Block block;
    final Event cause;
    boolean cancelled = false;

    @Override
    public void setCancelled(boolean c) {
        if (cause instanceof Cancellable) {
            ((Cancellable) cause).setCancelled(c);
        }
        cancelled = c;
    }

    public boolean hasEntity() {
        return entity != null;
    }

    public boolean hasBlock() {
        return block != null;
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
