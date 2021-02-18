package com.cavetale.worldmarker.block;

import org.bukkit.block.Block;

public interface BlockMarkerHook {
    void onBlockLoad(Block block, String id);

    void onBlockUnload(Block block, String id);

    default void onBlockSet(Block block, String id) { }

    default void onBlockReset(Block block) { }
}
