package com.cavetale.worldmarker;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Conveniently listen for some actions which are otherwise
 * inconvenient to listen for.
 */
final class EventListener implements Listener {
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
    void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        final ClickType click;
        switch (event.getAction()) {
        case RIGHT_CLICK_BLOCK:
        case RIGHT_CLICK_AIR:
            click = ClickType.RIGHT;
            break;
        case LEFT_CLICK_BLOCK:
        case LEFT_CLICK_AIR:
            click = ClickType.LEFT;
            break;
        default:
            return;
        }
        final EquipmentSlot hand = event.getHand();
        final Player player = event.getPlayer();
        final ItemStack item;
        if (hand == EquipmentSlot.HAND) {
            item = player.getInventory().getItemInMainHand();
        } else if (hand == EquipmentSlot.OFF_HAND) {
            item = player.getInventory().getItemInOffHand();
        } else {
            return;
        }
        if (item == null) return;
        final String id = ItemMarker.getId(item);
        if (id == null) return;
        final Block block = event.getClickedBlock();
        MarkedItemUseEvent ev =
            new MarkedItemUseEvent(player, item, id, hand, click, null, block, event);
        Bukkit.getServer().getPluginManager().callEvent(ev);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final EquipmentSlot hand = event.getHand();
        final ItemStack item;
        if (hand == EquipmentSlot.HAND) {
            item = player.getInventory().getItemInMainHand();
        } else if (hand == EquipmentSlot.OFF_HAND) {
            item = player.getInventory().getItemInOffHand();
        } else {
            return;
        }
        if (item == null) return;
        final String id = ItemMarker.getId(item);
        if (id == null) return;
        final Entity entity = event.getRightClicked();
        final ClickType click = ClickType.RIGHT;
        MarkedItemUseEvent ev =
            new MarkedItemUseEvent(player, item, id, hand, click, entity, null, event);
        Bukkit.getServer().getPluginManager().callEvent(ev);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        final Player player = (Player) event.getDamager();
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        String id = ItemMarker.getId(item);
        if (id == null) return;
        final EquipmentSlot hand = EquipmentSlot.HAND;
        final Entity entity = event.getEntity();
        final ClickType click = ClickType.LEFT;
        MarkedItemUseEvent ev =
            new MarkedItemUseEvent(player, item, id, hand, click, entity, null, event);
        Bukkit.getServer().getPluginManager().callEvent(ev);
    }
}
