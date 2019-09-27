package com.cavetale.worldmarker;

import java.util.Map;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * The x, y, z coordinates represent **absolute** world coordinates,
 * not regional ones.  To get the coordinate within a region, compute
 * `x & 511 and `z & 511`.
 */
public final class MarkBlock {
    final MarkChunk markChunk;
    // World coordinates:
    public final int x;
    public final int y;
    public final int z;
    // Util.regional(x, y, z):
    public final int key;
    MarkTag tag;

    MarkBlock(@NonNull final MarkChunk markChunk,
              final int x, final int y, final int z, final int key,
              final MarkTag tag) {
        this.markChunk = markChunk;
        this.x = x;
        this.y = y;
        this.z = z;
        this.key = key;
        this.tag = tag;
    }

    public boolean hasId() {
        return tag != null && tag.id != null;
    }

    public void setId(final String id) {
        markChunk.markRegion.update();
        if (tag == null) tag = new MarkTag();
        tag.id = id;
    }

    public void resetId() {
        if (tag == null) return;
        markChunk.markRegion.update();
        tag.id = null;
    }

    public String getId() {
        if (tag == null) return null;
        return tag.id;
    }

    public boolean hasId(@NonNull String id) {
        if (tag == null) return false;
        return id.equals(tag.id);
    }

    public Map<String, Object> getData() {
        markChunk.markRegion.update(); // Potential update
        if (tag == null) tag = new MarkTag();
        return tag.getData();
    }

    public void reset() {
        tag = null;
    }

    public boolean isEmpty() {
        return tag == null || tag.isEmpty();
    }

    public Block getBlock() {
        return getWorld().getBlockAt(x, y, z);
    }

    public World getWorld() {
        return markChunk.markRegion.markWorld.getWorld();
    }

    /**
     * Return the distance to the nearest player in chunks (16 blocks).
     */
    public int getPlayerDistance() {
        return markChunk.playerDistance;
    }

    public int getTicksLoaded() {
        return markChunk.loadedTicks;
    }

    public boolean isValid() {
        return markChunk.isValid()
            && markChunk.blocks.get(key) == this;
    }
}
