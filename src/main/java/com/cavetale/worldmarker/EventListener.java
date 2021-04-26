package com.cavetale.worldmarker;

import com.cavetale.worldmarker.block.BlockMarker;
import com.cavetale.worldmarker.item.ItemMarker;
import com.cavetale.worldmarker.util.Util;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Conveniently listen for some actions which are otherwise
 * inconvenient to listen for.
 */
@RequiredArgsConstructor
final class EventListener implements Listener {
    private final WorldMarkerPlugin plugin;
    private Map<Block, String> poppedBlocks = new HashMap<>();

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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String id = ItemMarker.getId(item);
        if (id == null) return;
        BlockState state = event.getBlock().getState();
        if (!(state instanceof TileState)) return;
        TileState tile = (TileState) state;
        PersistentDataContainer tag = tile.getPersistentDataContainer();
        tag.set(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING, id);
        tile.update();
    }

    /**
     * Unfreeze a stored item id from a broken block. See
     * `onBlockPlace()` for more details.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockDropItem(BlockDropItemEvent event) {
        BlockState state = event.getBlockState();
        if (!(state instanceof TileState)) return;
        TileState tile = (TileState) state;
        PersistentDataContainer tag = tile.getPersistentDataContainer();
        if (!tag.has(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING)) return;
        String id = tag.get(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING);
        if (event.getItems().size() != 1) return; // necessary?
        ItemStack item = event.getItems().get(0).getItemStack();
        if (ItemMarker.hasId(item)) return;
        ItemMarker.setId(item, id);
    }

    private void onBlockPop(final Block block) {
        BlockState state = block.getState();
        if (!(state instanceof TileState)) return;
        TileState tile = (TileState) state;
        PersistentDataContainer tag = tile.getPersistentDataContainer();
        if (!tag.has(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING)) return;
        String id = tag.get(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING);
        poppedBlocks.put(block, id);
        Bukkit.getScheduler().runTask(plugin, () -> {
                String id2 = poppedBlocks.remove(block);
                if (id2 != null) {
                    plugin.getLogger().warning(id + ", " + id2 + ": unused popped block: " + Util.toString(block));
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().getType() != Material.WATER) return;
        onBlockPop(event.getToBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            onBlockPop(block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            onBlockPop(block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            onBlockPop(block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onItemSpawn(ItemSpawnEvent event) {
        String id = poppedBlocks.remove(event.getLocation().getBlock());
        if (id == null) return;
        ItemStack item = event.getEntity().getItemStack();
        if (item.getAmount() != 1) return; // necessary?
        ItemMarker.setId(item, id);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() instanceof JavaPlugin) {
            BlockMarker.onUnload((JavaPlugin) event.getPlugin());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onChunkLoad(ChunkLoadEvent event) {
        BlockMarker.onChunkLoad(event.getChunk());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onChunkUnload(ChunkUnloadEvent event) {
        BlockMarker.onChunkUnload(event.getChunk());
    }
}
