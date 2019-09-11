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
    final TreeMap<Integer, MarkBlock> blocks = new TreeMap<>();
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
            while (null != (line = in.readLine())) {
                try {
                    if (line.startsWith("block,")) {
                        String[] toks = line.split(",", 5);
                        int x = Integer.parseInt(toks[1]);
                        int y = Integer.parseInt(toks[2]);
                        int z = Integer.parseInt(toks[3]);
                        int blockKey = Util.regional(x, y, z);
                        MarkBlock.Tag tag = gson.fromJson(toks[4], MarkBlock.Tag.class);
                        MarkBlock markBlock = new MarkBlock(this, x, y, z, blockKey, tag);
                        blocks.put(blockKey, markBlock);
                    } else {
                        markWorld.plugin.getLogger()
                            .info("MarkRegion: " + file + ": Invalid line: `" + line + "'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void save() {
        lastSave = System.nanoTime();
        try (PrintStream out = new PrintStream(file)) {
            for (MarkBlock markBlock : blocks.values()) {
                if (markBlock.isEmpty()) continue;
                out.println("block,"
                            + markBlock.x + "," + markBlock.y + "," + markBlock.z
                            + "," + gson.toJson(markBlock.tag));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    MarkBlock getBlock(final int x, final int y, final int z) {
        int blockKey = Util.regional(x, y, z);
        MarkBlock result = blocks.get(blockKey);
        if (result == null) {
            result = new MarkBlock(this, x, y, z, blockKey, null);
            blocks.put(blockKey, result);
        }
        return result;
    }
}
