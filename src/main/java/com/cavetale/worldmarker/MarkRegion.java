package com.cavetale.worldmarker;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.TreeMap;

/**
 * Each instance of this class represents a region file stored in
 * `$WORLD/cavetale.markers/r.$X.$Z${SUFFIX}'.
 */
final class MarkRegion {
    final MarkWorld markWorld;
    final int rx;
    final int rz;
    final long key;
    final File file;
    TreeMap<Long, MarkChunk> chunks = new TreeMap<>();
    static Gson gson = new Gson();
    static final String SUFFIX = ".json";
    transient long lastUse;
    transient long lastUpdate;
    transient long lastSave;

    MarkRegion(final MarkWorld markWorld,
               final int x, final int z, final long key) {
        this.markWorld = markWorld;
        this.rx = x;
        this.rz = z;
        this.key = key;
        file = new File(markWorld.folder, "r." + x + "." + z + SUFFIX);
        if (file.exists()) load();
        lastUse = Util.now();
        lastSave = lastUse;
        lastUpdate = lastUse;
    }

    void use() {
        lastUse = Util.now();
    }

    void update() {
        lastUpdate = Util.now();
        lastUse = lastUpdate;
    }

    /**
     * Return seconds between last save and last use.
     */
    long getNoSave() {
        return Util.now() - lastSave;
    }

    /**
     * Return seconds between last use and now.
     */
    long getNoUse() {
        return Util.now() - lastUse;
    }

    /**
     * Return seconds between last update and now.
     */
    long getNoUpdate() {
        return Util.now() - lastUpdate;
    }

    boolean isDirty() {
        return lastUpdate > lastSave;
    }

    void load() {
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
                        MarkTag tag = gson.fromJson(toks[3], MarkTag.class);
                        MarkChunk markChunk = new MarkChunk(this, x, z, chunkKey, tag);
                        chunks.put(chunkKey, markChunk);
                    } else if (line.startsWith("block,")) {
                        String[] toks = line.split(",", 5);
                        int x = Integer.parseInt(toks[1]);
                        int y = Integer.parseInt(toks[2]);
                        int z = Integer.parseInt(toks[3]);
                        int blockKey = Util.regional(x, y, z);
                        MarkTag tag = gson.fromJson(toks[4], MarkTag.class);
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

    void save() {
        lastSave = Util.now();
        try (PrintStream out = new PrintStream(file)) {
            for (MarkChunk markChunk : chunks.values()) {
                if (markChunk.isEmpty()) continue;
                out.println("chunk,"
                            + markChunk.x + "," + markChunk.z
                            + "," + gson.toJson(markChunk.tag));
                for (MarkBlock markBlock : markChunk.blocks.values()) {
                    if (markBlock.isEmpty()) continue;
                    out.println("block,"
                                + markBlock.x + "," + markBlock.y + "," + markBlock.z
                                + "," + gson.toJson(markBlock.tag));
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
