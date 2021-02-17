package com.cavetale.worldmarker.block;

import com.cavetale.worldmarker.WorldMarkerPlugin;
import com.cavetale.worldmarker.util.Tags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlockMarker {
    private static final Map<Integer, NamespacedKey> LEVEL_KEY_MAP = new TreeMap<>();
    private static final Map<NamespacedKey, Integer> KEY_LEVEL_MAP = new HashMap<>();
    private static final NamespacedKey[][][] BLOCK_KEY_ARRAY = new NamespacedKey[16][16][16];
    private static final Map<NamespacedKey, int[]> KEY_BLOCK_MAP = new HashMap<>();
    private static final List<BlockMarkerSlot> SLOTS = new ArrayList<>();
    private static boolean chunkScanScheduled = false;

    private BlockMarker() { }

    public static void enable(WorldMarkerPlugin plugin) {
        for (int i = 0; i < 16; i += 1) {
            NamespacedKey key = new NamespacedKey(plugin, "level." + i);
            LEVEL_KEY_MAP.put(i, key);
            KEY_LEVEL_MAP.put(key, i);
        }
        for (int y = 0; y < 16; y += 1) {
            for (int z = 0; z < 16; z += 1) {
                for (int x = 0; x < 16; x += 1) {
                    NamespacedKey key = new NamespacedKey(plugin, "" + x + "." + y + "." + z);
                    BLOCK_KEY_ARRAY[y][z][x] = key;
                    KEY_BLOCK_MAP.put(key, new int[] {x, y, z});
                }
            }
        }
    }

    /**
     * Get the tag belonging to a block, optionally creating it.
     * @param block the block
     * @param create create missing sections?
     * @param callback The function which can modify the located
     * tag. The return value determines if changes should be saved or
     * not.
     */
    public static PersistentDataContainer getTag(Block block, boolean create, Function<PersistentDataContainer, Boolean> callback) {
        PersistentDataContainer tag = block.getChunk().getPersistentDataContainer();
        int level = block.getY() >> 4;
        NamespacedKey levelKey = LEVEL_KEY_MAP.get(level);
        PersistentDataContainer levelTag;
        if (!tag.has(levelKey, PersistentDataType.TAG_CONTAINER)) {
            if (!create) return null;
            levelTag = tag.getAdapterContext().newPersistentDataContainer();
        } else {
            levelTag = tag.get(levelKey, PersistentDataType.TAG_CONTAINER);
        }
        final int ix = block.getX() & 0xF;
        final int iy = block.getY() & 0xF;
        final int iz = block.getZ() & 0xF;
        NamespacedKey blockKey = BLOCK_KEY_ARRAY[iy][iz][ix];
        PersistentDataContainer blockTag;
        if (!levelTag.has(blockKey, PersistentDataType.TAG_CONTAINER)) {
            if (!create) return null;
            blockTag = levelTag.getAdapterContext().newPersistentDataContainer();
        } else {
            blockTag = levelTag.get(blockKey, PersistentDataType.TAG_CONTAINER);
        }
        if (callback != null) {
            if (callback.apply(blockTag)) {
                if (blockTag.isEmpty()) {
                    levelTag.remove(blockKey);
                } else {
                    levelTag.set(blockKey, PersistentDataType.TAG_CONTAINER, blockTag);
                }
                if (levelTag.isEmpty()) {
                    tag.remove(levelKey);
                } else {
                    tag.set(levelKey, PersistentDataType.TAG_CONTAINER, levelTag);
                }
            }
        }
        return blockTag;
    }

    public static String getId(@NonNull Block block) {
        PersistentDataContainer blockTag = getTag(block, false, null);
        return blockTag != null ? Tags.getString(blockTag, WorldMarkerPlugin.ID_KEY) : null;
    }

    public static void setId(@NonNull Block block, @NonNull String id) {
        getTag(block, true, tag -> {
                Tags.set(tag, WorldMarkerPlugin.ID_KEY, id);
                return true;
            });
    }

    public static void resetId(@NonNull Block block) {
        getTag(block, false, tag -> {
                tag.remove(WorldMarkerPlugin.ID_KEY);
                return true;
            });
    }

    public static boolean hasId(@NonNull Block block) {
        return getId(block) != null;
    }

    public static boolean hasId(@NonNull Block block, @NonNull String id) {
        return id.equals(getId(block));
    }

    public static Map<Block, String> getAllBlockIds(Chunk chunk) {
        Map<Block, String> result = new HashMap<>();
        PersistentDataContainer tag = chunk.getPersistentDataContainer();
        for (NamespacedKey levelKey : tag.getKeys()) {
            Integer level = KEY_LEVEL_MAP.get(levelKey);
            if (level == null) continue;
            if (!tag.has(levelKey, PersistentDataType.TAG_CONTAINER)) continue;
            PersistentDataContainer levelTag = tag.get(levelKey, PersistentDataType.TAG_CONTAINER);
            for (NamespacedKey blockKey : levelTag.getKeys()) {
                int[] vec = KEY_BLOCK_MAP.get(blockKey);
                if (vec == null) continue;
                if (!levelTag.has(blockKey, PersistentDataType.TAG_CONTAINER)) continue;
                PersistentDataContainer blockTag = levelTag.get(blockKey, PersistentDataType.TAG_CONTAINER);
                if (!blockTag.has(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING)) continue;
                final int ix = vec[0];
                final int iy = vec[1];
                final int iz = vec[2];
                Block block = chunk.getBlock(ix, iy + (level << 4), iz);
                String id = blockTag.get(WorldMarkerPlugin.ID_KEY, PersistentDataType.STRING);
                result.put(block, id);
            }
        }
        return result;
    }

    /**
     * Register a new hook to receive calls when chunks with block ids
     * in them are loaded.  This will trigger a scan of all chunks for
     * existing blocks ids, which will also call the hook.
     */
    public static void registerHook(@NonNull JavaPlugin plugin, @NonNull BlockMarkerHook hook) {
        SLOTS.add(new BlockMarkerSlot(plugin, hook));
        if (chunkScanScheduled) return;
        chunkScanScheduled = true;
        Bukkit.getScheduler().runTask(WorldMarkerPlugin.getInstance(), () -> {
                chunkScanScheduled = false;
                Map<Block, String> blockIdMap = scanAllChunks();
                for (BlockMarkerSlot slot : SLOTS) {
                    if (slot.didScanAllChunks) continue;
                    slot.didScanAllChunks = true;
                    try {
                        for (Map.Entry<Block, String> entry : blockIdMap.entrySet()) {
                            slot.hook.onBlockLoad(entry.getKey(), entry.getValue());
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
    }

    public static Map<Block, String> scanAllChunks() {
        Map<Block, String> blockIdMap = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                blockIdMap.putAll(getAllBlockIds(chunk));
            }
        }
        return blockIdMap;
    }

    public static void onUnload(JavaPlugin plugin) {
        SLOTS.removeIf(s -> s.plugin == plugin);
    }

    public static void onChunkLoad(Chunk chunk) {
        Map<Block, String> blockIdMap = getAllBlockIds(chunk);
        for (BlockMarkerSlot slot : SLOTS) {
            try {
                for (Map.Entry<Block, String> entry : blockIdMap.entrySet()) {
                    slot.hook.onBlockLoad(entry.getKey(), entry.getValue());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void onChunkUnload(Chunk chunk) {
        Map<Block, String> blockIdMap = getAllBlockIds(chunk);
        for (BlockMarkerSlot slot : SLOTS) {
            try {
                for (Map.Entry<Block, String> entry : blockIdMap.entrySet()) {
                    slot.hook.onBlockUnload(entry.getKey(), entry.getValue());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
