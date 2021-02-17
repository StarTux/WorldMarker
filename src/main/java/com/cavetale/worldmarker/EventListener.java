package com.cavetale.worldmarker;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Conveniently listen for some actions which are otherwise
 * inconvenient to listen for.
 */
@RequiredArgsConstructor
final class EventListener implements Listener {
    private final WorldMarkerPlugin plugin;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        tag.set(WorldMarkerPlugin.idKey, PersistentDataType.STRING, id);
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
        if (!tag.has(WorldMarkerPlugin.idKey, PersistentDataType.STRING)) return;
        String id = tag.get(WorldMarkerPlugin.idKey, PersistentDataType.STRING);
        if (event.getItems().size() != 1) return;
        ItemStack item = event.getItems().get(0).getItemStack();
        if (ItemMarker.hasId(item)) return;
        ItemMarker.setId(item, id);
    }
}
