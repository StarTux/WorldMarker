package com.cavetale.worldmarker;

import java.util.Collection;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new BlockMarker(this);
        new ItemMarker(this);
        new EntityMarker(this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getScheduler().runTaskTimer(this, this::onTick, 1, 1);
    }

    @Override
    public void onDisable() {
    }

    void onTick() {
        BlockMarker.instance.onTick();
    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String alias,
                             final String[] args) {
        if (args.length == 0) return false;
        switch (args[0]) {
        case "block": {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player expected!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length > 2) return false;
            Block block = player.getLocation().getBlock();
            String at = "" + block.getX() + "," + block.getY() + "," + block.getZ();
            if (args.length == 1) {
                String id = BlockMarker.getId(block);
                if (id == null) {
                    player.sendMessage("No id stored at " + at + "!");
                } else {
                    player.sendMessage(at + ": " + id);
                }
            } else if (args.length == 2) {
                String id = args[1];
                BlockMarker.setId(block, id);
                player.sendMessage(at + " => " + id);
            }
            return true;
        }
        case "chunk": {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player expected!");
                return true;
            }
            Player player = (Player) sender;
            Chunk chunk = player.getLocation().getChunk();
            Collection<MarkBlock> markBlocks = BlockMarker.getBlocks(chunk);
            player.sendMessage("" + markBlocks.size() + " marked blocks in chunk "
                               + chunk.getX() + "," + chunk.getZ() + ".");
            for (MarkBlock markBlock : markBlocks) {
                player.sendMessage(markBlock.x + "," + markBlock.y + "," + markBlock.z
                                   + ": " + markBlock.getId());
            }
            return true;
        }
        case "item": {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player expected!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length > 2) return false;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getAmount() == 0) {
                player.sendMessage("No item in hand!");
                return true;
            }
            String it = item.getType().name().toLowerCase();
            if (args.length == 1) {
                String id = ItemMarker.getId(item);
                player.sendMessage(it + ": " + id);
            } else if (args.length == 2) {
                String id = args[1];
                ItemMarker.setId(item, id);
                player.sendMessage(it + " => " + id);
            }
            return true;
        }
        case "entity": {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player expected!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length > 2) return false;
            Entity entity = player.getNearbyEntities(1.0, 1.0, 1.0).stream()
                .filter(e -> e != player)
                .findFirst().orElse(null);
            if (entity == null) {
                player.sendMessage("No entity!");
                return true;
            }
            String it = entity.getType().name().toLowerCase();
            if (args.length == 1) {
                String id = EntityMarker.getId(entity);
                player.sendMessage(it + ": " + id);
            } else if (args.length == 2) {
                String id = args[1];
                EntityMarker.setId(entity, id);
                player.sendMessage(it + " => " + id);
            }
            return true;
        }
        case "save": {
            BlockMarker.instance.saveAll();
            sender.sendMessage("All regions saved!");
            return true;
        }
        default: return false;
        }
    }
}
