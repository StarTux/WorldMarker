package com.cavetale.worldmarker;

import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    final Json json = new Json(this);
    static WorldMarkerPlugin instance;
    final BlockMarker blockMarker = new BlockMarker(this);
    final ItemMarker itemMarker = new ItemMarker(this);
    final EntityMarker entityMarker = new EntityMarker(this);

    @Override
    public void onEnable() {
        instance = this;
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
        blockMarker.onTick();
    }
}
