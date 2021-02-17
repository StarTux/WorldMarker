package com.cavetale.worldmarker;

import com.cavetale.worldmarker.block.BlockMarker;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    @Getter static WorldMarkerPlugin instance;
    @SuppressWarnings("Deprecation")
    public static final NamespacedKey ID_KEY = new NamespacedKey("worldmarker", "id");
    final WorldMarkerCommand command = new WorldMarkerCommand(this);
    final EventListener eventListener = new EventListener(this);

    @Override
    public void onEnable() {
        instance = this;
        BlockMarker.enable(this);
        eventListener.enable();
        getCommand("worldmarker").setExecutor(command);
    }

    @Override
    public void onDisable() { }
}
