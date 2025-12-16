package me.gabichigg.pathfinder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

// /pathdelete <nombre> - Elimina destino
class PathDeleteCommand implements CommandExecutor, TabCompleter {
    private final PathfinderGPS plugin;

    public PathDeleteCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pathfinder.delete")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathdelete.usage"));
            return true;
        }

        String destName = args[0];

        if (plugin.getPathManager().deleteDestination(destName)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathdelete.success", "name", destName));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathdelete.not_found", "name", destName));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(plugin.getPathManager().getAllDestinationNames());
        }
        return null;
    }
}
