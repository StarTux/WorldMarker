package com.cavetale.worldmarker;

import java.util.Arrays;
import java.util.Collection;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class WorldMarkerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String alias,
                             final String[] args) {
        if (args.length == 0) return false;
        String[] argl = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
        case "block": return blockCommand(sender, argl);
        case "chunk": return chunkCommand(sender, argl);
        case "item": return itemCommand(sender, argl);
        case "entity": return entityCommand(sender, argl);
        case "info": return infoCommand(sender, argl);
        case "save": {
            BlockMarker.instance.saveAll();
            sender.sendMessage("All regions saved!");
            return true;
        }
        default: return false;
        }
    }

    boolean blockCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length > 1) return false;
        Player player = (Player) sender;
        Block block = player.getLocation().getBlock();
        String at = "" + block.getX() + "," + block.getY() + "," + block.getZ();
        if (args.length == 0) {
            String id = BlockMarker.getId(block);
            if (id == null) {
                player.sendMessage("No id stored at " + at + "!");
            } else {
                player.sendMessage(at + ": " + id);
            }
            player.sendMessage(at
                               + " valid=" + BlockMarker.getBlock(block).isValid()
                               + " empty=" + BlockMarker.getBlock(block).isEmpty());
        } else if (args.length == 1) {
            String id = args[0];
            BlockMarker.setId(block, id);
            player.sendMessage(at + " => " + id);
        }
        return true;
    }

    boolean itemCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length > 1) return false;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getAmount() == 0) {
            player.sendMessage("No item in hand!");
            return true;
        }
        String it = item.getType().name().toLowerCase();
        if (args.length == 0) {
            String id = ItemMarker.getId(item);
            player.sendMessage(it + ": " + id);
        } else if (args.length == 1) {
            String id = args[0];
            ItemMarker.setId(item, id);
            player.sendMessage(it + " => " + id);
        }
        return true;
    }

    boolean entityCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length > 1) return false;
        Player player = (Player) sender;
        Entity entity = player.getNearbyEntities(1.0, 1.0, 1.0).stream()
            .filter(e -> e != player)
            .findFirst().orElse(null);
        if (entity == null) {
            player.sendMessage("No entity!");
            return true;
        }
        String it = entity.getType().name().toLowerCase();
        if (args.length == 0) {
            String id = EntityMarker.getId(entity);
            player.sendMessage(it + ": " + id);
        } else if (args.length == 1) {
            String id = args[0];
            EntityMarker.setId(entity, id);
            player.sendMessage(it + " => " + id);
        }
        return true;
    }

    boolean chunkCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected!");
            return true;
        }
        Player player = (Player) sender;
        Chunk chunk = player.getLocation().getChunk();
        MarkChunk markChunk = BlockMarker.getChunk(chunk);
        player.sendMessage("Chunk " + markChunk.x + " " + markChunk.z + ":"
                           + " valid=" + markChunk.isValid()
                           + " loaded=" + markChunk.loaded
                           + " pdist=" + markChunk.playerDistance
                           + " ticks=" + markChunk.loadedTicks
                           + " region=" + markChunk.markRegion.rx + "," + markChunk.markRegion.rz
                           + " orig=" + chunk.getX() + "," + chunk.getZ()
                           + " key=" + Util.xFromLong(markChunk.key) + "," + Util.zFromLong(markChunk.key));
        if (args.length == 1 && args[0].equals("blocks")) {
            Collection<MarkBlock> markBlocks = BlockMarker.getBlocks(chunk);
            player.sendMessage("" + markBlocks.size() + " marked blocks in chunk "
                               + chunk.getX() + "," + chunk.getZ() + ".");
            for (MarkBlock markBlock : markBlocks) {
                player.sendMessage(markBlock.x + "," + markBlock.y + "," + markBlock.z
                                   + ": " + markBlock.getId());
            }
        }
        return true;
    }

    boolean infoCommand(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        sender.sendMessage("" + BlockMarker.instance.worlds.size() + " worlds loaded:");
        for (MarkWorld markWorld : BlockMarker.instance.worlds.values()) {
            sender.sendMessage(""
                               + markWorld.getWorld().getName() + ": "
                               + markWorld.loadedChunks.size() + " chunks loaded, "
                               + markWorld.regions.size() + " regions:");
            for (MarkRegion markRegion : markWorld.regions.values()) {
                ChatColor r = ChatColor.RESET;
                ChatColor c = ChatColor.YELLOW;
                sender.sendMessage(" "
                                   + markRegion.getIdString()
                                   + " " + c + markRegion.chunks.size() + r + " chunks"
                                   + " dirty=" + c + markRegion.isDirty() + r
                                   + " empty=" + c + markRegion.isEmpty() + r
                                   + " noUse=" + c + markRegion.getNoUse() + r
                                   + " noSave=" + c + markRegion.getNoSave());
            }
        }
        return true;
    }
}
