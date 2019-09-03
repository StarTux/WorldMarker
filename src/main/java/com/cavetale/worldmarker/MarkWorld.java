package com.cavetale.worldmarker;

import java.io.File;
import java.util.TreeMap;
import lombok.NonNull;
import org.bukkit.World;

public final class MarkWorld {
    final File folder;
    final TreeMap<Long, MarkRegion> regions = new TreeMap<>();

    MarkWorld(@NonNull final World world) {
        folder = new File(world.getWorldFolder(), "cavetale.markers");
        folder.mkdirs();
    }

    private MarkRegion getMarkRegion(final int x, final int z) {
        long key = Util.toLong(x, z);
        MarkRegion result = regions.get(key);
        if (result == null) {
            result = new MarkRegion(this, x, z, key);
            regions.put(key, result);
        }
        result.use();
        return result;
    }

    public MarkBlock getMarkBlock(final int x, final int y, final int z) {
        return getMarkRegion(x >> 9, z >> 9).getMarkBlock(x & 511, y, z & 511);
    }
}
