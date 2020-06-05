package com.cavetale.worldmarker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.TreeMap;

/**
 * Each instance of this class represents a region file stored in
 * `$WORLD/cavetale.markers/r.$X.$Z${SUFFIX}'.
 *
 * An instance of this should not be cached across server ticks
 * because it might become invalidated.  Always use
 * `MarkWorld::getRegion()` to receive a valid copy.
 */
final class MarkRegion {
    final MarkWorld markWorld;
    final int rx;
    final int rz;
    final long key;
    final File file;
    final TreeMap<Long, MarkChunk> chunks = new TreeMap<>();
    static final String SUFFIX = ".json";
    transient long lastUse;
    transient long lastSave;
    transient boolean dirty;

    MarkRegion(final MarkWorld markWorld,
               final int x, final int z, final long key) {
        this.markWorld = markWorld;
        this.rx = x;
        this.rz = z;
        this.key = key;
        file = new File(markWorld.folder, "r." + x + "." + z + SUFFIX);
        if (file.exists()) loadFromDisk();
        lastUse = Util.nowInSeconds();
        lastSave = lastUse;
    }

    /**
     * Mark this region as currently in use.  It will stay in use for
     * at least 60 seconds.  This gets called on every tick as long as
     * players keep chunks within it loaded.  It's also used when a
     * region is requested.  All this happens in MarkWorld.
     */
    void use() {
        lastUse = Util.nowInSeconds();
    }

    /**
     * Mark this region to be in need of save.  This is called by the
     * save functions of every MarkChunk or MarkRegion within this
     * region.  It is up to the client plugin to call save() after
     * modifying raw data.
     */
    void save() {
        use();
        dirty = true;
    }

    /**
     * Return seconds between last save and last use.
     */
    long getNoSave() {
        return Util.nowInSeconds() - lastSave;
    }

    /**
     * Return seconds between last use and now.
     */
    long getNoUse() {
        return Util.nowInSeconds() - lastUse;
    }

    void loadFromDisk() {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            int linum = 0;
            while (null != (line = in.readLine())) {
                linum += 1;
                try {
                    if (line.startsWith("chunk,")) {
                        String[] toks = line.split(",", 4);
                        int x = Integer.parseInt(toks[1]);
                        int z = Integer.parseInt(toks[2]);
                        long chunkKey = Util.toLong(x, z);
                        MarkTag tag = markWorld.plugin.json.deserialize(toks[3], MarkTag.class);
                        MarkChunk markChunk = new MarkChunk(this, x, z, chunkKey, tag);
                        chunks.put(chunkKey, markChunk);
                    } else if (line.startsWith("block,")) {
                        String[] toks = line.split(",", 5);
                        int x = Integer.parseInt(toks[1]);
                        int y = Integer.parseInt(toks[2]);
                        int z = Integer.parseInt(toks[3]);
                        int blockKey = Util.regional(x, y, z);
                        MarkTag tag = markWorld.plugin.json.deserialize(toks[4], MarkTag.class);
                        MarkChunk markChunk = getChunk(x >> 4, z >> 4);
                        MarkBlock markBlock = new MarkBlock(markChunk, x, y, z, blockKey, tag);
                        markChunk.blocks.put(blockKey, markBlock);
                    } else {
                        markWorld.plugin.getLogger()
                            .warning("MarkRegion: " + file + ": Invalid line "
                                     + linum + ": `" + line + "'");
                    }
                } catch (Exception e) {
                    markWorld.plugin.getLogger()
                        .warning("MarkRegion: " + file + ": Line "
                                 + linum + ": `" + line + "'");
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void writeToDisk() {
        dirty = false;
        lastSave = Util.nowInSeconds();
        try (PrintStream out = new PrintStream(file)) {
            for (MarkChunk markChunk : chunks.values()) {
                if (markChunk.isEmpty()) continue;
                markChunk.prepareForSaving();
                if (markChunk.hasTag()) {
                    out.println("chunk,"
                                + markChunk.x + "," + markChunk.z
                                + "," + Json.serialize(markChunk.tag));
                }
                for (MarkBlock markBlock : markChunk.blocks.values()) {
                    if (markBlock.isEmpty()) continue;
                    markBlock.prepareForSaving();
                    if (markBlock.hasTag()) {
                        out.println("block,"
                                    + markBlock.x + "," + markBlock.y + "," + markBlock.z
                                    + "," + Json.serialize(markBlock.tag));
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    MarkChunk getChunk(final int x, final int z) {
        long chunkKey = Util.toLong(x, z);
        MarkChunk result = chunks.get(chunkKey);
        if (result == null) {
            result = new MarkChunk(this, x, z, chunkKey, null);
            chunks.put(chunkKey, result);
        }
        return result;
    }

    MarkBlock getBlock(final int x, final int y, final int z) {
        return getChunk(x >> 4, z >> 4).getBlock(x, y, z);
    }

    public boolean isEmpty() {
        for (MarkChunk markChunk : chunks.values()) {
            if (!markChunk.isEmpty()) return false;
        }
        return true;
    }

    public String getIdString() {
        return rx + "," + rz;
    }

    public boolean isValid() {
        return markWorld.isValid()
            && markWorld.regions.get(key) == this;
    }
}
