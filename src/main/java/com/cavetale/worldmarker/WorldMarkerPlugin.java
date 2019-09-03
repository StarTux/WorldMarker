package com.cavetale.worldmarker;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldMarkerPlugin extends JavaPlugin {
    BlockMarker blockMarker;

    @Override
    public void onEnable() {
        blockMarker = new BlockMarker();
    }

    @Override
    public void onDisable() {
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
                player.sendMessage(at + ": " + id);
            } else if (args.length == 2) {
                String id = args[1];
                BlockMarker.setId(block, id);
                player.sendMessage(at + " => " + id);
            }
            return true;
        }
        case "save": {
            BlockMarker.instance.saveAll();
            sender.sendMessage("All regions saved!");
        }
        default: return false;
        }
    }
}
