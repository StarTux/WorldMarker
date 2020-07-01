package com.cavetale.worldmarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public final class MarkChunk extends MarkTagContainer {
    final MarkRegion markRegion;
    public final int x;
    public final int z;
    public final long key;
    final TreeMap<Integer, MarkBlock> blocks = new TreeMap<>();
    boolean loaded = false;
    int playerDistance = Integer.MAX_VALUE;
    int loadedTicks = 0;
    long lastUse;

    MarkChunk(@NonNull final MarkRegion markRegion,
              final int x, final int z, final long key,
              final MarkTag tag) {
        this.markRegion = markRegion;
        this.x = x;
        this.z = z;
        this.key = key;
        this.tag = tag;
    }

    MarkBlock getBlock(final int bx, final int by, final int bz) {
        int blockKey = Util.regional(bx, by, bz);
        MarkBlock result = blocks.get(blockKey);
        if (result == null) {
            result = new MarkBlock(this, bx, by, bz, blockKey, null);
            blocks.put(blockKey, result);
        }
        return result;
    }

    public boolean isEmpty() {
        if (!super.isEmpty()) return false;
        for (MarkBlock markBlock : blocks.values()) {
            if (!markBlock.isEmpty()) return false;
        }
        return true;
    }

    public Collection<MarkBlock> getBlocks() {
        return new ArrayList<>(blocks.values());
    }

    public Stream<MarkBlock> streamBlocksWithId(@NonNull String id) {
        return getBlocks().stream()
            .filter(b -> b.hasId(id));
    }

    public int getTicksLoaded() {
        return loadedTicks;
    }

    public boolean isValid() {
        return markRegion.isValid()
            && markRegion.chunks.get(key) == this;
    }

    /**
     * Call every now and then?
     */
    void cleanUp() {
        for (Iterator<MarkBlock> iter = blocks.values().iterator(); iter.hasNext();) {
            if (iter.next().isEmpty()) iter.remove();
        }
    }

    public MarkWorld getWorld() {
        return markRegion.markWorld;
    }

    public Chunk getChunk() {
        return getWorld().getWorld().getChunkAt(x, z);
    }

    /**
     * Save at the next interval.
     */
    @Override
    public void save() {
        markRegion.save();
    }

    /**
     * Mark this chunk as currently in use.  It will stay in use for
     * at least 60 seconds.  This gets called on every tick as long as
     * players keep chunks within it loaded.  It's also used when a
     * chunk is requested.  All this happens in MarkWorld.
     */
    void use() {
        lastUse = Util.nowInSeconds();
    }

    @Override
    void onUnload() {
        super.onUnload();
        for (MarkBlock block : blocks.values()) block.onUnload();
        MarkChunkUnloadEvent event = new MarkChunkUnloadEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public String getCoordString() {
        return x + "," + z;
    }

    void onTick() {
        for (MarkBlock block : blocks.values()) {
            block.onTick();
        }
        super.onTick();
    }

    @Override
    public String toString() {
        return "MarkChunk(" + getWorld().getName() + ":" + x + "," + z + ")";
    }

    public boolean isBukkitChunkLoaded() {
        return getWorld().getWorld().isChunkLoaded(x, z);
    }

    @Override
    protected void tickTickable(Tickable tickable) {
        tickable.onTickMarkChunk(this);
    }
}
