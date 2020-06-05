package com.cavetale.worldmarker;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onChunkLoad(ChunkLoadEvent event) {
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onChunkUnload(ChunkUnloadEvent event) {
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        BlockMarker.instance.unloadWorld(world);
    }

    /**
     * For items which can be placed, if the corresponding block is a
     * TileState, "freeze" the item id in the TileState.
     * This block will not be treated like a marked block because the
     * latter uses its own custom storage. The benefit of this is that
     * marked items can be placed an picked up again. Namely, this
     * goes for player heads.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String id = ItemMarker.getId(item);
        if (id == null) return;
        BlockState state = event.getBlock().getState();
        if (!(state instanceof TileState)) return;
        TileState tile = (TileState) state;
        PersistentDataContainer tag = tile.getPersistentDataContainer();
        tag.set(ItemMarker.instance.key, PersistentDataType.STRING, id);
        tile.update();
    }

    /**
     * Unfreeze a stored item id from a broken block. See
     * `onBlockPlace()` for more details.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        BlockState state = event.getBlockState();
        if (!(state instanceof TileState)) return;
        TileState tile = (TileState) state;
        PersistentDataContainer tag = tile.getPersistentDataContainer();
        if (!tag.has(ItemMarker.instance.key, PersistentDataType.STRING)) return;
        String id = tag.get(ItemMarker.instance.key, PersistentDataType.STRING);
        if (event.getItems().size() != 1) return;
        ItemStack item = event.getItems().get(0).getItemStack();
        if (ItemMarker.hasId(item)) return;
        ItemMarker.setId(item, id);
    }

    public static final class Debug implements Persistent {
        String test = "";

        @Override
        public void onTick() {
            System.out.println("HERE!");
        }
    }

    @EventHandler
    public void onMarkChunkLoad(MarkChunkLoadEvent event) {
        event.getChunk().streamBlocksWithId("debug")
            .forEach(b -> {
                    System.out.println("Load debug at " + b.getCoordString());
                    Debug debug = b.getPersistent("debug", Debug.class, Debug::new);
                });
    }

    @EventHandler
    public void onMarkChunkTick(MarkChunkTickEvent event) {
    }
}
