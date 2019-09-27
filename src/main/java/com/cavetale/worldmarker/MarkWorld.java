package com.cavetale.worldmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class MarkWorld {
    final WorldMarkerPlugin plugin;
    final World world;
    final UUID uid;
    final File folder;
    final TreeMap<Long, MarkRegion> regions = new TreeMap<>();
    final TreeMap<Long, MarkChunk> loadedChunks = new TreeMap<>();

    MarkWorld(@NonNull final WorldMarkerPlugin plugin, @NonNull final World world) {
        this.plugin = plugin;
        this.world = world;
        this.uid = world.getUID();
        folder = new File(world.getWorldFolder(), "cavetale.markers");
        folder.mkdirs();
    }

    /**
     * Called by BlockMarker::onTick via WorldMarkerPlugin::onTick task.
     */
    void onTick() {
        // Update player distance for all loaded chunks
        List<Integer> ps = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            Location loc = player.getLocation();
            ps.add(loc.getBlockX() >> 4);
            ps.add(loc.getBlockZ() >> 4);
        }
        for (MarkChunk markChunk : new ArrayList<>(loadedChunks.values())) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < ps.size(); i += 2) {
                int x = ps.get(i);
                int z = ps.get(i + 1);
                int d = Math.max(Math.abs(x - markChunk.x),
                                 Math.abs(z - markChunk.z));
                if (d < min) min = d;
            }
            markChunk.playerDistance = min;
            markChunk.markRegion.use();
            if (!markChunk.isEmpty()) {
                MarkChunkTickEvent event = new MarkChunkTickEvent(markChunk);
                plugin.getServer().getPluginManager().callEvent(event);
                markChunk.loadedTicks += 1;
            }
        }
        for (Iterator<MarkRegion> iter = regions.values().iterator(); iter.hasNext();) {
            MarkRegion markRegion = iter.next();
            if (markRegion.isDirty() && markRegion.getNoSave() >= 60L) {
                markRegion.save();
            }
            // If a region has not been used at all for x seconds, unload it.
            if (markRegion.getNoUse() > 10L) {
                if (markRegion.isDirty()) {
                    markRegion.save();
                }
                iter.remove();
            }
        }
    }

    void saveAll() {
        for (MarkRegion markRegion : regions.values()) {
            if (markRegion.isDirty()) markRegion.save();
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

    void markChunkLoaded(Chunk chunk) {
        MarkChunk markChunk = getChunk(chunk);
        loadedChunks.put(markChunk.key, markChunk);
        markChunk.loaded = true;
        markChunk.playerDistance = Integer.MAX_VALUE;
        markChunk.loadedTicks = 0;
    }

    void markChunkUnloaded(Chunk chunk) {
        MarkChunk markChunk = getChunk(chunk);
        loadedChunks.remove(markChunk.key);
        markChunk.loaded = false;
        markChunk.playerDistance = Integer.MAX_VALUE;
        markChunk.loadedTicks = 0;
        markChunk.cleanUp();
    }

    MarkChunk getChunk(@NonNull Chunk chunk) {
        return getChunk(chunk.getX(), chunk.getZ());
    }

    MarkChunk getChunk(final int x, final int z) {
        return getRegion(x >> 5, z >> 5).getChunk(x, z);
    }

    public MarkBlock getBlock(final int x, final int y, final int z) {
        return getRegion(x >> 9, z >> 9).getBlock(x, y, z);
    }

    public Collection<MarkBlock> getBlocksWithin(final int ax, final int ay, final int az,
                                                 final int bx, final int by, final int bz) {
        List<MarkBlock> result = new ArrayList<>();
        // Chunk Area
        final int cax = ax >> 4;
        final int caz = az >> 4;
        final int cbx = bx >> 4;
        final int cbz = bz >> 4;
        // Region Area
        final int rax = ax >> 9;
        final int raz = az >> 9;
        final int rbx = bx >> 9;
        final int rbz = bz >> 9;
        for (int rz = raz; rz <= rbz; rz += 1) {
            for (int rx = rax; rx <= rbx; rx += 1) {
                MarkRegion markRegion = getRegion(rx, rz);
                for (MarkChunk markChunk : markRegion.chunks.values()) {
                    if (markChunk.x < cax || markChunk.x > cbx
                        || markChunk.z < caz || markChunk.z > cbz) {
                        continue;
                    }
                    for (MarkBlock markBlock : markChunk.blocks.values()) {
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
        }
        return result;
    }

    public World getWorld() {
        return world;
    }

    public boolean isValid() {
        return BlockMarker.instance.worlds.get(uid) == this;
    }
}
