package com.cavetale.worldmarker;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    final Json json = new Json(this);
    static WorldMarkerPlugin instance;
    final BlockMarker blockMarker = new BlockMarker(this);
    final ItemMarker itemMarker = new ItemMarker(this);
    final EntityMarker entityMarker = new EntityMarker(this);
    final WorldMarkerCommand command = new WorldMarkerCommand(this);

    @Override
    public void onEnable() {
        instance = this;
        MarkTag.idKey = new NamespacedKey(this, "id");
        MarkTag.dataKey = new NamespacedKey(this, "data");
        BlockMarker.instance.loadAllWorlds();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getScheduler().runTaskTimer(this, this::onTick, 1, 1);
        getCommand("worldmarker").setExecutor(command);
        getServer().getScheduler().runTask(this, entityMarker::scanAllWorlds);
    }

    @Override
    public void onDisable() {
        blockMarker.saveAll();
        entityMarker.saveAll();
        blockMarker.clear();
        entityMarker.clear();
    }

    void onTick() {
        blockMarker.onTick();
        entityMarker.onTick();
    }
}
