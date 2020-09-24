package com.cavetale.worldmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MarkWorld extends MarkTagContainer {
    final WorldMarkerPlugin plugin;
    private final World world;
    private final UUID uid;
    final File folder;
    private final File file;
    final TreeMap<Long, MarkRegion> regions = new TreeMap<>();
    final TreeMap<Long, MarkChunk> loadedChunks = new TreeMap<>();
    static final String FN = "world.json";
    private transient boolean dirty;
    private transient long lastSave;

    MarkWorld(@NonNull final WorldMarkerPlugin plugin, @NonNull final World world) {
        this.plugin = plugin;
        this.world = world;
        this.uid = world.getUID();
        folder = new File(world.getWorldFolder(), "cavetale.markers");
        file = new File(folder, FN);
        folder.mkdirs();
        loadFromDisk();
    }

    /**
     * Called by BlockMarker::onTick via WorldMarkerPlugin::onTick task.
     *
     * Saves regions and the world to disk if they're dirty and have
     * not been saved for 60 seconds.
     */
    void onTick() {
        // Update player distance
        final int viewDistance = world.getViewDistance();
        List<Integer> players = new ArrayList<>();
        Set<Long> viewChunks = new TreeSet<>();
        for (Player player : world.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;
            if (!player.isValid()) continue;
            Location loc = player.getLocation();
            final int cx = loc.getBlockX() >> 4;
            final int cz = loc.getBlockZ() >> 4;
            players.add(cx);
            players.add(cz);
            for (int dz = -viewDistance; dz <= viewDistance; dz += 1) {
                for (int dx = -viewDistance; dx <= viewDistance; dx += 1) {
                    long key = Util.toLong(cx + dx, cz + dz);
                    viewChunks.add(key);
                }
            }
        }
        // Make sure that all chunks within view distance are loaded
        for (Long key : viewChunks) {
            int x = Util.xFromLong(key);
            int z = Util.zFromLong(key);
            if (!world.isChunkLoaded(x, z)) continue;
            // Will add the chunk to loadedChunks and call use()
            MarkChunk markChunk = getChunk(x, z);
        }
        long now = Util.nowInSeconds();
        // Tick all active chunks.
        for (MarkChunk markChunk : new ArrayList<>(loadedChunks.values())) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < players.size(); i += 2) {
                int x = players.get(i);
                int z = players.get(i + 1);
                int d = Math.max(Math.abs(x - markChunk.x),
                                 Math.abs(z - markChunk.z));
                if (d < min) min = d;
            }
            markChunk.playerDistance = min;
            markChunk.loadedTicks += 1;
            markChunk.markRegion.use();
            boolean shouldTick = markChunk.playerDistance <= viewDistance
                && !markChunk.isEmpty()
                && world.isChunkLoaded(markChunk.x, markChunk.z);
            if (shouldTick) {
                markChunk.onTick();
                MarkChunkTickEvent event = new MarkChunkTickEvent(markChunk);
                plugin.getServer().getPluginManager().callEvent(event);
            }
            long noUse = now - markChunk.lastUse;
            if (noUse > 60L) {
                markChunk.onUnload();
                loadedChunks.remove(markChunk.key);
            }
        }
        // Save regions if necessary and unload unused ones
        for (Iterator<MarkRegion> iter = regions.values().iterator(); iter.hasNext();) {
            MarkRegion markRegion = iter.next();
            if (markRegion.dirty && markRegion.getNoSave() > 300L) {
                markRegion.writeToDisk();
            }
            // If a region has not been used at all for x seconds, unload it.
            long noUse = now - markRegion.lastUse;
            if (noUse > 60L) {
                if (markRegion.dirty) {
                    markRegion.writeToDisk();
                }
                iter.remove();
            }
        }
        // Save world
        if (dirty && getNoSave() >= 300L) {
            writeToDisk();
        }
        super.onTick();
    }

    void saveAll() {
        for (MarkRegion markRegion : regions.values()) {
            if (markRegion.dirty) markRegion.writeToDisk();
        }
        if (dirty) {
            writeToDisk();
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

    private void loadFromDisk() {
        tag = plugin.json.load(file, MarkTag.class, MarkTag::new);
    }

    private void writeToDisk() {
        dirty = false;
        lastSave = Util.nowInSeconds();
        prepareForSaving();
        plugin.json.save(file, tag, false);
    }

    MarkChunk getChunk(@NonNull Chunk chunk) {
        return getChunk(chunk.getX(), chunk.getZ());
    }

    MarkChunk getChunk(final int x, final int z) {
        Long key = Util.toLong(x, z);
        MarkChunk chunk = loadedChunks.get(key);
        if (chunk != null) {
            chunk.use();
            return chunk;
        }
        chunk = getRegion(x >> 5, z >> 5).getChunk(x, z);
        loadedChunks.put(key, chunk);
        chunk.use();
        MarkChunkLoadEvent event = new MarkChunkLoadEvent(chunk);
        plugin.getServer().getPluginManager().callEvent(event);
        return chunk;
    }

    long getNoSave() {
        return Util.nowInSeconds() - lastSave;
    }

    public MarkBlock getBlock(final int x, final int y, final int z) {
        return getChunk(x >> 4, z >> 4).getBlock(x, y, z);
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

    @Override
    public void save() {
        dirty = true;
        lastSave = Util.nowInSeconds();
    }

    void onUnload() {
        super.onUnload();
        for (MarkChunk chunk : loadedChunks.values()) chunk.onUnload();
    }

    public String getName() {
        return world.getName();
    }

    @Override
    public String toString() {
        return "MarkWorld(" + getName() + ")";
    }

    @Override
    protected void tickTickable(Tickable tickable) {
        tickable.onTickMarkWorld(this);
    }

    public void onPluginDisable(JavaPlugin javaPlugin) {
        removePlugin(javaPlugin);
        for (MarkChunk markChunk : loadedChunks.values()) {
            markChunk.onPluginDisable(javaPlugin);
        }
    }

    public List<MarkBlock> getBlocksWithId(@NonNull String id) {
        List<MarkBlock> list = new ArrayList<>();
        for (MarkChunk markChunk : loadedChunks.values()) {
            list.addAll(markChunk.getBlocksWithId(id));
        }
        return list;
    }
}
