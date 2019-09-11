package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * The x, y, z coordinates represent **absolute** world coordinates,
 * not regional ones.  To get the coordinate within a region, compute
 * `x & 511 and `z & 511`.
 */
@AllArgsConstructor
public final class MarkBlock {
    final MarkRegion markRegion;
    // World coordinates:
    public final int x;
    public final int y;
    public final int z;
    // Util.regional(x, y, z):
    public final int key;
    Tag tag;

    static class Tag {
        private String id;
        private Map<String, Object> data;
    }

    public boolean hasId() {
        return tag != null && tag.id != null;
    }

    public void setId(final String id) {
        markRegion.update();
        if (tag == null) tag = new Tag();
        tag.id = id;
    }

    public void resetId() {
        if (tag == null) return;
        markRegion.update();
        tag.id = null;
    }

    public String getId() {
        if (tag == null) return null;
        return tag.id;
    }

    public Map<String, Object> getData() {
        markRegion.update(); // Potential update
        if (tag == null) tag = new Tag();
        if (tag.data == null) tag.data = new HashMap<>();
        return tag.data;
    }

    public void reset() {
        tag = null;
    }

    public boolean isEmpty() {
        if (tag == null) return true;
        return tag.id == null
            && (tag.data == null || tag.data.isEmpty());
    }

    public Block getBlock() {
        return getWorld().getBlockAt(x, y, z);
    }

    public World getWorld() {
        return markRegion.markWorld.getWorld();
    }
}
