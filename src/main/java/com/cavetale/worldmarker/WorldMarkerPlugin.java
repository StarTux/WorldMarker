package com.cavetale.worldmarker;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    final Json json = new Json(this);
    @Getter static WorldMarkerPlugin instance;
    BlockMarker blockMarker;
    ItemMarker itemMarker;
    EntityMarker entityMarker;
    final WorldMarkerCommand command = new WorldMarkerCommand(this);
    final EventListener eventListener = new EventListener(this);

    @Override
    public void onEnable() {
        instance = this;
        MarkTag.idKey = new NamespacedKey(this, "id");
        MarkTag.dataKey = new NamespacedKey(this, "data");
        itemMarker = new ItemMarker(this).enable();
        blockMarker = new BlockMarker(this).enable();
        entityMarker = new EntityMarker(this).enable();
        eventListener.enable();
        getCommand("worldmarker").setExecutor(command);
    }

    @Override
    public void onDisable() {
        blockMarker.saveAll();
        entityMarker.saveAll();
        blockMarker.clear();
        entityMarker.clear();
    }

    void onPluginDisable(JavaPlugin plugin) {
        blockMarker.onPluginDisable(plugin);
        //        entityMarker.onPluginDisable(plugin);
    }
}
