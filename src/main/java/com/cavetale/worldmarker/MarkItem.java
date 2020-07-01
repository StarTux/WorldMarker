package com.cavetale.worldmarker;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * This extension of MarkTagContainer can only ever exist transiently
 * because items can disappear and reappear from one tick to the next.
 * Therefore, saving is done instantaneously. An instance should never
 * be cached, and the Transient framework should not be used.
 */
public final class MarkItem extends MarkTagContainer {
    final WorldMarkerPlugin plugin;
    public final ItemStack item;

    MarkItem(final WorldMarkerPlugin plugin, final ItemStack item) {
        this.plugin = plugin;
        this.item = item;
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            tag = MarkTag.load(meta);
        }
    }

    @Override
    public void save() {
        if (isEmpty() && !item.hasItemMeta()) return;
        prepareForSaving();
        ItemMeta meta = item.getItemMeta();
        MarkTag.save(tag, meta);
        item.setItemMeta(meta);
    }

    /**
     * Item's Persistents cannot be ticked because they only ever
     * exist temporarily.
     */
    @Override
    protected void tickTickable(Tickable tickable) {
    }
}
