package com.cavetale.worldmarker;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.TreeMap;

final class MarkRegion {
    private final MarkWorld markWorld;
    private final int rx;
    private final int rz;
    private final long key;
    private final File file;
    private final TreeMap<Integer, MarkBlock> blocks = new TreeMap<>();
    static Gson gson = new Gson();
    transient long lastUse;
    transient long lastSave;

    MarkRegion(final MarkWorld markWorld,
               final int x, final int z, final long key) {
        this.markWorld = markWorld;
        this.rx = x;
        this.rz = z;
        this.key = key;
        file = new File(markWorld.folder, "r." + x + "." + z + ".json");
        if (file.exists()) load();
        lastUse = System.nanoTime();
    }

    void use() {
        lastUse = System.nanoTime();
    }

    void load() {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while (null != (line = in.readLine())) {
                try {
                    String[] toks = line.split(",", 4);
                    int x = Integer.parseInt(toks[0]);
                    int y = Integer.parseInt(toks[1]);
                    int z = Integer.parseInt(toks[2]);
                    int blockKey = Util.regional(x, y, z);
                    MarkBlock markBlock = gson.fromJson(toks[3], MarkBlock.class);
                    markBlock.setXYZK(x, y, z, blockKey);
                    blocks.put(blockKey, markBlock);
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
                out.println(markBlock.x + "," + markBlock.y + "," + markBlock.z + ","
                            + gson.toJson(markBlock));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    MarkBlock getMarkBlock(final int x, final int y, final int z) {
        int blockKey = Util.regional(x, y, z);
        MarkBlock result = blocks.get(blockKey);
        if (result == null) {
            result = new MarkBlock();
            result.setXYZK(x, y, z, blockKey);
            blocks.put(blockKey, result);
        }
        return result;
    }
}
