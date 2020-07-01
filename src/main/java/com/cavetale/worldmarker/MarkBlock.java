package com.cavetale.worldmarker;

import lombok.NonNull;
import org.bukkit.block.Block;

/**
 * The x, y, z coordinates represent **absolute** world coordinates,
 * not regional ones.  To get the coordinate within a region, compute
 * `x & 511 and `z & 511`.
 *
 * An instance of this should not be cached across server ticks
 * because it might become invalidated.  Always use
 * `MarkWorld::getBlock()` to receive a valid copy.
 */
public final class MarkBlock extends MarkTagContainer {
    final MarkChunk markChunk;
    // World coordinates:
    public final int x;
    public final int y;
    public final int z;
    // Util.regional(x, y, z):
    public final int key;

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

    public Block getBlock() {
        return getWorld().getWorld().getBlockAt(x, y, z);
    }

    public MarkWorld getWorld() {
        return markChunk.markRegion.markWorld;
    }

    public MarkChunk getChunk() {
        return markChunk;
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

    @Override
    public void save() {
        markChunk.markRegion.save();
    }

    @Override
    public String toString() {
        return "MarkBlock(" + getWorld().getName() + ":" + x + "," + y + "," + z + ")";
    }

    public String getCoordString() {
        return "" + x + "," + y + "," + z;
    }

    public boolean isBukkitChunkLoaded() {
        return getChunk().isBukkitChunkLoaded();
    }

    @Override
    protected void tickTickable(Tickable tickable) {
        tickable.onTickMarkBlock(this);
    }
}
