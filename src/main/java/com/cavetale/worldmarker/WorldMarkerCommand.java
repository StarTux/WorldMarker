package com.cavetale.worldmarker;

import java.util.Arrays;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class WorldMarkerCommand implements CommandExecutor {
    final WorldMarkerPlugin plugin;

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
        case "debug": return debugCommand(sender, argl);
        default: return false;
        }
    }

    boolean blockCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length > 2) return false;
        Player player = (Player) sender;
        Block block = player.getTargetBlock(6);
        if (block == null) {
            player.sendMessage("No target block!");
            return true;
        }
        MarkBlock markBlock = BlockMarker.getBlock(block);
        if (args.length == 0) {
            player.sendMessage("Tag at " + markBlock.getCoordString() + ": " + markBlock.getTag());
        } else if (args.length >= 1) {
            String id = args[0];
            markBlock.setId(id);
            if ("debug".equals(id)) {
                markBlock.getPersistent("debug", EventListener.Debug.class, EventListener.Debug::new)
                    .test = args.length >= 2 ? args[1] : "-";
            }
            markBlock.save();
            player.sendMessage("Set tag at " + markBlock.getCoordString() + " to " + id);
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
        if (args.length > 2) return false;
        Player player = (Player) sender;
        Entity entity = player.getTargetEntity(6);
        if (entity == null) {
            player.sendMessage("No target entity");
            return true;
        }
        String it = entity.getType().name().toLowerCase();
        MarkEntity markEntity = EntityMarker.getEntity(entity);
        if (args.length == 0) {
            player.sendMessage("Tag of " + it + ": " + markEntity.getTag());
        } else if (args.length >= 1) {
            String id = args[0];
            markEntity.setId(id);
            if ("debug".equals(id) && args.length >= 1) {
                markEntity.getPersistent("debug", EventListener.Debug.class, EventListener.Debug::new)
                    .test = args.length >= 2 ? args[1] : "-";
            }
            markEntity.save();
            player.sendMessage("Set tag of " + it + ": " + markEntity.getTag());
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
                           + " key=" + Util.xFromLong(markChunk.key) + ","
                           + Util.zFromLong(markChunk.key));
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
                                   + " dirty=" + c + markRegion.dirty + r
                                   + " empty=" + c + markRegion.isEmpty() + r
                                   + " noUse=" + c + markRegion.getNoUse() + r
                                   + " noSave=" + c + markRegion.getNoSave());
            }
        }
        return true;
    }

    boolean debugCommand(CommandSender sender, String[] args) {
        return false;
    }
}
