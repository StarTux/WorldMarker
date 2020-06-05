package com.cavetale.worldmarker;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Conveniently listen for some actions which are otherwise
 * inconvenient to listen for.
 */
final class EventListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        BlockMarker.instance.unloadWorld(world);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onEntityAddToWorld(EntityAddToWorldEvent event) {
        EntityMarker.getEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        EntityMarker.instance.exit(event.getEntity());
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
        tag.set(MarkTag.idKey, PersistentDataType.STRING, id);
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
        if (!tag.has(MarkTag.idKey, PersistentDataType.STRING)) return;
        String id = tag.get(MarkTag.idKey, PersistentDataType.STRING);
        if (event.getItems().size() != 1) return;
        ItemStack item = event.getItems().get(0).getItemStack();
        if (ItemMarker.hasId(item)) return;
        ItemMarker.setId(item, id);
    }

    public static final class Debug implements Persistent {
        String test = "";
        int ticks = 0;

        Debug() {
            WorldMarkerPlugin.instance.getLogger().info("worldmarker::Debug::constructor");
        }

        @Override
        public void onUnload(MarkTagContainer container) {
            WorldMarkerPlugin.instance.getLogger().info("worldmarker::Debug::onUnload " + container + " " + test);
        }

        @Override
        public void onSave(MarkTagContainer container) {
            WorldMarkerPlugin.instance.getLogger().info("worldmarker::Debug::onSave " + container + " " + test);
        }

        @Override
        public void onTick(MarkTagContainer container) {
            if (!container.hasId("debug")) {
                container.removePersistent("debug");
                container.save();
                return;
            }
            if (ticks % 20 == 0) {
                WorldMarkerPlugin.instance.getLogger().info("worldmarker::Debug::onTick " + container + " " + test);
            }
            ticks += 1;
        }
    }

    @EventHandler
    public void onMarkChunkLoad(MarkChunkLoadEvent event) {
        event.getChunk().streamBlocksWithId("debug")
            .forEach(b -> {
                    b.getPersistent("debug", Debug.class, Debug::new);
                });
    }

    @EventHandler
    public void onMarkEntityLoad(MarkEntityLoadEvent event) {
        if (event.getEntity().hasId("debug")) {
            event.getEntity().getPersistent("debug", Debug.class, Debug::new);
        }
    }
}
