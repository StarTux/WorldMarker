package com.cavetale.worldmarker;

import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class MarkEntity extends MarkTagContainer {
    final WorldMarkerPlugin plugin;
    final Entity entity;
    boolean dirty;
    long lastSave;

    MarkEntity(final WorldMarkerPlugin plugin, final Entity entity) {
        this.plugin = plugin;
        this.entity = entity;
        try {
            tag = MarkTag.load(entity);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "MarkEnttiy::load", e);
        }
        lastSave = Util.nowInSeconds();
    }

    @Override
    public void save() {
        dirty = true;
    }

    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    void serializeToEntityTag() {
        lastSave = Util.nowInSeconds();
        prepareForSaving(); // calls onSave
        MarkTag.save(tag, entity);
    }

    @Override
    public String toString() {
        Location loc = entity.getLocation();
        return "MarkEntity(" + entity.getType().name().toLowerCase()
            + "|" + loc.getWorld().getName()
            + ":" + loc.getBlockX()
            + "," + loc.getBlockY()
            + "," + loc.getBlockZ()
            + ")";
    }

    @Override
    protected void tickTickable(Tickable tickable) {
        tickable.onTickMarkEntity(this);
    }
}
