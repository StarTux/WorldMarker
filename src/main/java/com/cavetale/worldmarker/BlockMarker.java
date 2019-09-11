package com.cavetale.worldmarker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class BlockMarker {
    static BlockMarker instance;
    final WorldMarkerPlugin plugin;
    final HashMap<UUID, MarkWorld> worlds = new HashMap<>();
    final Stack<Runnable> tasks = new Stack<>();

    BlockMarker(@NonNull final WorldMarkerPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    void onTick() {
        if (tasks.isEmpty()) {
            for (UUID uuid : worlds.keySet()) {
                tasks.push(() -> tickWorld(uuid));
            }
            return;
        }
        tasks.pop().run();
    }

    // Task
    void tickWorld(UUID uuid) {
        MarkWorld markWorld = worlds.get(uuid);
        if (markWorld == null) return;
        for (Long key : markWorld.regions.keySet()) {
            tasks.push(() -> markWorld.tickRegion(key));
        }
    }

    void saveAll() {
        for (MarkWorld markWorld : worlds.values()) {
            for (MarkRegion markRegion : markWorld.regions.values()) {
                markRegion.save();
            }
        }
    }

    public static MarkWorld getWorld(@NonNull World world) {
        MarkWorld result = instance.worlds.get(world.getUID());
        if (result == null) {
            result = new MarkWorld(instance.plugin, world);
            instance.worlds.put(world.getUID(), result);
        }
        return result;
    }

    public static MarkWorld getWorld(@NonNull String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) return null;
        return getWorld(world);
    }

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

    public static void resetId(@NonNull Block block, @NonNull String id) {
        getBlock(block).resetId();
    }

    public static Collection<MarkBlock> getBlocksWithin(@NonNull Block a,
                                                        @NonNull Block b) {
        if (!a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("Worlds do not match!");
        }
        MarkWorld markWorld = getWorld(a.getWorld());
        return markWorld.getBlocksWithin(a.getX(), a.getY(), a.getZ(),
                                         b.getX(), b.getY(), b.getZ());
    }

    public static Collection<MarkBlock> getNearbyBlocks(@NonNull Block center,
                                                        final int radius) {
        return getBlocksWithin(center.getRelative(-radius, -radius, -radius),
                               center.getRelative(radius, radius, radius));
    }

    public static Collection<MarkBlock> getBlocks(@NonNull Chunk chunk) {
        return getBlocksWithin(chunk.getBlock(0, 0, 0), chunk.getBlock(15, 255, 15));
    }
}
