package com.cavetale.worldmarker;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public final class EntityMarker {
    static EntityMarker instance;
    final WorldMarkerPlugin plugin;
    final Map<Integer, MarkEntity> cache = new TreeMap<>();

    EntityMarker(@NonNull final WorldMarkerPlugin plugin) {
        instance = this;
        this.plugin = plugin;
    }

    void saveAll() {
        for (MarkEntity entity : cache.values()) {
            if (entity.dirty) entity.serializeToEntityTag();
        }
    }

    void clear() {
        cache.clear();
    }

    void exit(Entity entity) {
        MarkEntity cached = cache.get(entity.getEntityId());
        if (cached == null) return;
        cached.onUnload();
        if (cached.dirty) cached.serializeToEntityTag();
        cache.remove(entity.getEntityId());
    }

    public static MarkEntity getEntity(Entity entity) {
        MarkEntity markEntity = instance.cache.get(entity.getEntityId());
        if (markEntity != null) return markEntity;
        markEntity = new MarkEntity(instance.plugin, entity);
        instance.cache.put(entity.getEntityId(), markEntity);
        MarkEntityLoadEvent event = new MarkEntityLoadEvent(markEntity);
        Bukkit.getPluginManager().callEvent(event);
        return markEntity;
    }

    public static void setId(@NonNull Entity entity, @NonNull String id) {
        getEntity(entity).setId(id);
    }

    public static void resetId(@NonNull Entity entity) {
        getEntity(entity).resetId();
    }

    public static String getId(@NonNull Entity entity) {
        return getEntity(entity).getId();
    }

    public static boolean hasId(@NonNull Entity entity, @NonNull String id) {
        return getEntity(entity).hasId(id);
    }

    void scanAllWorlds() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                getEntity(entity);
            }
        }
    }

    void onTick() {
        long now = Util.nowInSeconds();
        for (MarkEntity entity : new ArrayList<>(cache.values())) {
            entity.onTick();
            if (entity.dirty) {
                long noSave = now - entity.lastSave;
                if (noSave > 60L) {
                    entity.serializeToEntityTag();
                }
            }
        }
    }

    public Stream<MarkEntity> streamAllLoadedEntities() {
        return new ArrayList<>(cache.values()).stream();
    }
}
