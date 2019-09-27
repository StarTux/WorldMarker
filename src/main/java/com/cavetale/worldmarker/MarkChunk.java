package com.cavetale.worldmarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import lombok.NonNull;

final class MarkChunk {
    final MarkRegion markRegion;
    final int x;
    final int z;
    final long key;
    final TreeMap<Integer, MarkBlock> blocks = new TreeMap<>();
    MarkTag tag;
    boolean loaded = false;
    int playerDistance = Integer.MAX_VALUE;
    int loadedTicks = 0;

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
        if (tag != null && !tag.isEmpty()) return false;
        for (MarkBlock markBlock : blocks.values()) {
            if (!markBlock.isEmpty()) return false;
        }
        return true;
    }

    public Collection<MarkBlock> getBlocks() {
        return new ArrayList<>(blocks.values());
    }

    public MarkTag getTag() {
        if (tag == null) tag = new MarkTag();
        return tag;
    }

    public String getId() {
        if (tag == null) return null;
        return tag.id;
    }

    public void setId(@NonNull String id) {
        markRegion.update();
        getTag().id = id;
    }

    public void resetId() {
        markRegion.update();
        if (tag == null) return;
        tag.id = null;
    }

    public int getTicksLoaded() {
        return loadedTicks;
    }

    public boolean isValid() {
        return markRegion.isValid()
            && markRegion.chunks.get(key) == this;
    }

    void cleanUp() {
        for (Iterator<MarkBlock> iter = blocks.values().iterator(); iter.hasNext();) {
            if (iter.next().isEmpty()) iter.remove();
        }
    }
}
