package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

// /pathset - Da la herramienta para marcar DESTINO
class PathSetCommand implements CommandExecutor {
    private final PathfinderGPS plugin;

    public PathSetCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("pathfinder.set")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        player.getInventory().addItem(plugin.createPathTool());
        player.sendMessage(plugin.getLanguageManager().getMessage("pathset.tool_received"));
        player.sendMessage(plugin.getLanguageManager().getMessage("pathset.tool_usage"));

        return true;
    }
}