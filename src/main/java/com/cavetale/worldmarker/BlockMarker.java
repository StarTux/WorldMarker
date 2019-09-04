package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class BlockMarker {
    static BlockMarker instance;
    final HashMap<UUID, MarkWorld> worlds = new HashMap<>();

    BlockMarker() {
        instance = this;
    }

    void saveAll() {
        for (MarkWorld markWorld : worlds.values()) {
            for (MarkRegion markRegion : markWorld.regions.values()) {
                markRegion.save();
            }
        }
    }

    public static MarkWorld getMarkWorld(@NonNull World world) {
        MarkWorld result = instance.worlds.get(world.getUID());
        if (result == null) {
            result = new MarkWorld(world);
            instance.worlds.put(world.getUID(), result);
        }
        return result;
    }

    public static MarkWorld getMarkWorld(@NonNull String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) return null;
        return getMarkWorld(world);
    }

    public static MarkBlock getMarkBlock(@NonNull World world,
                                  final int x, final int y, final int z) {
        MarkWorld markWorld = getMarkWorld(world);
        if (markWorld == null) return null;
        return markWorld.getMarkBlock(x, y, z);
    }

    public static MarkBlock getMarkBlock(@NonNull Block block) {
        return getMarkBlock(block.getWorld(),
                            block.getX(), block.getY(), block.getZ());
    }

    public static String getId(@NonNull Block block) {
        return getMarkBlock(block).getId();
    }

    public static void setId(@NonNull Block block, @NonNull String id) {
        getMarkBlock(block).setId(id);
    }

    public static void resetId(@NonNull Block block, @NonNull String id) {
        getMarkBlock(block).setId(null);
    }
}
