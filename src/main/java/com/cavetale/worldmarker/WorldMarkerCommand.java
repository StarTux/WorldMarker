package com.cavetale.worldmarker;

import com.cavetale.worldmarker.block.BlockMarker;
import com.cavetale.worldmarker.entity.EntityMarker;
import com.cavetale.worldmarker.item.ItemMarker;
import com.cavetale.worldmarker.util.Tags;
import com.cavetale.worldmarker.util.Util;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
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
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) return false;
        String[] argl = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
        case "block": return blockCommand(sender, argl);
        case "item": return itemCommand(sender, argl);
        case "entity": return entityCommand(sender, argl);
        case "debug": return debugCommand(sender, argl);
        default: return false;
        }
    }

    boolean blockCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length == 0) return false;
        Player player = (Player) sender;
        Block block = player.getTargetBlockExact(6);
        if (block == null) {
            player.sendMessage("No target block!");
            return true;
        }
        switch (args[0]) {
        case "get":
            if (args.length != 1) return false;
            player.sendMessage("Tag at " + Util.toString(block) + ": " + BlockMarker.getId(block));
            return true;
        case "set": {
            if (args.length < 2) return false;
            String val = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            BlockMarker.setId(block, val);
            player.sendMessage("Tag at " + Util.toString(block) + " set to: " + BlockMarker.getId(block));
            return true;
        }
        case "reset":
            if (args.length != 1) return false;
            BlockMarker.resetId(block);
            player.sendMessage("Tag at " + Util.toString(block) + " set to: " + BlockMarker.getId(block));
            return true;
        case "list": {
            if (args.length != 1) return false;
            Chunk chunk = block.getChunk();
            Map<Block, String> map = BlockMarker.getAllBlockIds(chunk);
            player.sendMessage("Chunk at " + chunk.getX() + "," + chunk.getZ() + " has " + map.size() + " ids:");
            for (Map.Entry<Block, String> entry : map.entrySet()) {
                player.sendMessage(Util.toString(entry.getKey()) + ": " + entry.getValue());
            }
            return true;
        }
        case "debug": {
            if (args.length != 1) return false;
            Chunk chunk = block.getChunk();
            player.sendMessage("All container data in chunk " + chunk.getX() + "," + chunk.getZ() + ":");
            Gson gson = new Gson();
            for (Map.Entry<NamespacedKey, Object> entry : Tags.toMap(chunk.getPersistentDataContainer()).entrySet()) {
                player.sendMessage(entry.getKey() + ": " + gson.toJson(entry.getValue()));
            }
            return true;
        }
        default: return false;
        }
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
        if (args.length == 0) return false;
        Player player = (Player) sender;
        Entity entity = player.getTargetEntity(6);
        if (entity == null) {
            player.sendMessage("No target entity");
            return true;
        }
        String it = entity.getType().name().toLowerCase();
        switch (args[0]) {
        case "get":
            if (args.length != 1) return false;
            player.sendMessage("Tag of " + it + ": " + EntityMarker.getId(entity));
            return true;
        case "set": {
            if (args.length < 2) return false;
            String val = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            EntityMarker.setId(entity, val);
            player.sendMessage("Tag of " + it + " set to: " + EntityMarker.getId(entity));
            return true;
        }
        case "reset":
            if (args.length != 1) return false;
            EntityMarker.resetId(entity);
            player.sendMessage("Tag of " + it + " reset: " + EntityMarker.getId(entity));
            return true;
        default: return false;
        }
    }

    boolean debugCommand(CommandSender sender, String[] args) {
        return false;
    }
}
