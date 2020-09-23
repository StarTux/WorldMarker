package com.cavetale.worldmarker;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    final Json json = new Json(this);
    @Getter static WorldMarkerPlugin instance;
    final BlockMarker blockMarker = new BlockMarker(this);
    final ItemMarker itemMarker = new ItemMarker(this);
    final EntityMarker entityMarker = new EntityMarker(this);
    final WorldMarkerCommand command = new WorldMarkerCommand(this);
    final EventListener eventListener = new EventListener(this);

    @Override
    public void onEnable() {
        instance = this;
        MarkTag.idKey = new NamespacedKey(this, "id");
        MarkTag.dataKey = new NamespacedKey(this, "data");
        BlockMarker.instance.loadAllWorlds();
        eventListener.enable();
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
        itemMarker.onTick();
    }

    void onPluginDisable(JavaPlugin plugin) {
        blockMarker.onPluginDisable(plugin);
        //        entityMarker.onPluginDisable(plugin);
    }
}
