package com.cavetale.worldmarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public final class BlockMarker {
    static BlockMarker instance;
    final WorldMarkerPlugin plugin;
    final HashMap<UUID, MarkWorld> worlds = new HashMap<>();

    public BlockMarker enable() {
        instance = this;
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        loadAllWorlds();
        return this;
    }

    void loadAllWorlds() {
        for (World world : plugin.getServer().getWorlds()) {
            MarkWorld markWorld = getWorld(world);
        }
    }

    void tick() {
        for (World world : plugin.getServer().getWorlds()) {
            getWorld(world).onTick();
        }
    }

    void saveAll() {
        for (MarkWorld markWorld : worlds.values()) {
            markWorld.saveAll();
        }
    }

    void clear() {
        worlds.clear();
    }

    public static MarkWorld getWorld(@NonNull World world) {
        MarkWorld result = instance.worlds.get(world.getUID());
        if (result == null) {
            result = new MarkWorld(instance.plugin, world);
            instance.worlds.put(world.getUID(), result);
        }
        return result;
    }

    void unloadWorld(@NonNull World world) {
        MarkWorld markWorld = instance.worlds.get(world.getUID());
        if (markWorld == null) return;
        markWorld.onUnload();
        markWorld.saveAll();
        worlds.remove(world.getUID());
    }

    // World API

    public static MarkWorld getWorld(@NonNull String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) return null;
        return getWorld(world);
    }

    // Chunk API

    public static MarkChunk getChunk(@NonNull Chunk chunk) {
        return getWorld(chunk.getWorld()).getChunk(chunk.getX(), chunk.getZ());
    }

    public static String getId(@NonNull Chunk chunk) {
        return getChunk(chunk).getId();
    }

    public static void setId(@NonNull Chunk chunk, @NonNull String id) {
        getChunk(chunk).setId(id);
    }

    public static void resetId(@NonNull Chunk chunk) {
        getChunk(chunk).resetId();
    }

    // Block API

    public static MarkBlock getBlock(@NonNull World world,
                                     final int x, final int y, final int z) {
        MarkWorld markWorld = getWorld(world);
        if (markWorld == null) return null;
        return markWorld.getBlock(x, y, z);
    }

    public static MarkBlock getBlock(@NonNull Block block) {
        return getBlock(block.getWorld(),
                        block.getX(), block.getY(), block.getZ());
    }

    public static String getId(@NonNull Block block) {
        return getBlock(block).getId();
    }

    public static void setId(@NonNull Block block, @NonNull String id) {
        getBlock(block).setId(id);
    }

    public static void resetId(@NonNull Block block) {
        getBlock(block).resetId();
    }

    public static boolean hasId(@NonNull Block block, @NonNull String id) {
        return getBlock(block).hasId(id);
    }

    public static boolean hasId(@NonNull Block block) {
        return getBlock(block).hasId();
    }

    public static Collection<MarkBlock> getBlocksWithin(@NonNull Block a, @NonNull Block b) {
        if (!a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("Worlds do not match!");
        }
        MarkWorld markWorld = getWorld(a.getWorld());
        return markWorld.getBlocksWithin(a.getX(), a.getY(), a.getZ(),
                                         b.getX(), b.getY(), b.getZ());
    }

    public static Collection<MarkBlock> getNearbyBlocks(@NonNull Block center, final int radius) {
        return getBlocksWithin(center.getRelative(-radius, -radius, -radius),
                               center.getRelative(radius, radius, radius));
    }

    public static Collection<MarkBlock> getBlocks(@NonNull Chunk chunk) {
        return getWorld(chunk.getWorld()).getChunk(chunk.getX(), chunk.getZ()).getBlocks();
    }

    public static Stream<MarkChunk> streamAllLoadedChunks() {
        return instance.worlds.values().stream()
            .flatMap(w -> w.loadedChunks.values().stream());
    }

    public void onPluginDisable(JavaPlugin javaPlugin) {
        for (MarkWorld markWorld : worlds.values()) {
            markWorld.onPluginDisable(javaPlugin);
        }
    }

    public List<MarkBlock> getBlocksWithId(@NonNull String id) {
        List<MarkBlock> list = new ArrayList<>();
        for (MarkWorld markWorld : worlds.values()) {
            list.addAll(markWorld.getBlocksWithId(id));
        }
        return list;
    }
}
