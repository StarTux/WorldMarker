package com.cavetale.worldmarker;

import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new BlockMarker(this);
        new ItemMarker(this);
        new EntityMarker(this);
        BlockMarker.instance.loadAllWorlds();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getScheduler().runTaskTimer(this, this::onTick, 1, 1);
        getCommand("worldmarker").setExecutor(new WorldMarkerCommand());
    }

    @Override
    public void onDisable() {
        BlockMarker.instance.saveAll();
        BlockMarker.instance.worlds.clear();
    }

    void onTick() {
        BlockMarker.instance.onTick();
    }
}
