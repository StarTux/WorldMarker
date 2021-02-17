package com.cavetale.worldmarker.util;

import org.bukkit.block.Block;

public final class Util {
    private Util() { }

    public static String toString(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
