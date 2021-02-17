package com.cavetale.worldmarker;

import org.bukkit.block.Block;

public final class Util {
    private Util() { }

    static String toString(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
