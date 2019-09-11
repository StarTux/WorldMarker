package com.cavetale.worldmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import lombok.NonNull;
import org.bukkit.World;

public final class MarkWorld {
    final WorldMarkerPlugin plugin;
    final World world;
    final File folder;
    final TreeMap<Long, MarkRegion> regions = new TreeMap<>();

    MarkWorld(@NonNull final WorldMarkerPlugin plugin, @NonNull final World world) {
        this.plugin = plugin;
        this.world = world;
        folder = new File(world.getWorldFolder(), "cavetale.markers");
        folder.mkdirs();
    }

    /**
     * Called by WorldMarker::tickWorld via onTick() task.
     */
    void tickRegion(long key) {
        MarkRegion markRegion = regions.get(key);
        if (markRegion == null) return;
        if (markRegion.isDirty() && markRegion.getNoSave() >= 60L) {
            markRegion.save();
        }
        // If a region has not been used at all for 2 minutes, unload it.
        if (markRegion.getNoUse() > 120L) {
            if (markRegion.isDirty()) {
                markRegion.save();
            }
            regions.remove(key);
        }
    }

    private MarkRegion getRegion(final int x, final int z) {
        long key = Util.toLong(x, z);
        MarkRegion result = regions.get(key);
        if (result == null) {
            result = new MarkRegion(this, x, z, key);
            regions.put(key, result);
        }
        result.use();
        return result;
    }

    public MarkBlock getBlock(final int x, final int y, final int z) {
        return getRegion(x >> 9, z >> 9).getBlock(x, y, z);
    }

    public Collection<MarkBlock> getBlocksWithin(final int ax, final int ay, final int az,
                                                 final int bx, final int by, final int bz) {
        List<MarkBlock> result = new ArrayList<>();
        final int rax = ax >> 9;
        final int raz = az >> 9;
        final int rbx = bx >> 9;
        final int rbz = bz >> 9;
        for (int rz = raz; rz <= rbz; rz += 1) {
            for (int rx = rax; rx <= rbx; rx += 1) {
                MarkRegion markRegion = getRegion(rx, rz);
                for (MarkBlock markBlock : markRegion.blocks.values()) {
                    if (markBlock.x < ax || markBlock.x > bx
                        || markBlock.y < ay || markBlock.y > by
                        || markBlock.z < az || markBlock.z > bz) {
                        continue;
                    }
                    if (markBlock.isEmpty()) continue;
                    result.add(markBlock);
                }
            }
        }
        return result;
    }

    public World getWorld() {
        return world;
    }
}
