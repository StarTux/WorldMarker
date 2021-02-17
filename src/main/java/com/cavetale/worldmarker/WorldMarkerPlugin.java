package com.cavetale.worldmarker;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    @Getter static WorldMarkerPlugin instance;
    final WorldMarkerCommand command = new WorldMarkerCommand(this);
    final EventListener eventListener = new EventListener(this);
    static NamespacedKey idKey;
    static NamespacedKey dataKey;

    @Override
    public void onEnable() {
        instance = this;
        idKey = new NamespacedKey(this, "id");
        dataKey = new NamespacedKey(this, "data");
        BlockMarker.enable(this);
        eventListener.enable();
        getCommand("worldmarker").setExecutor(command);
    }

    @Override
    public void onDisable() {
    }
}
