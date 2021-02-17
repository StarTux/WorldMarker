package com.cavetale.worldmarker.block;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
final class BlockMarkerSlot {
    protected final JavaPlugin plugin;
    protected final BlockMarkerHook hook;
    protected boolean didScanAllChunks = false;
}
