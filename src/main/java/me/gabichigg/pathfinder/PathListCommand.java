package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

// /pathlist - Lista destinos
class PathListCommand implements CommandExecutor {
    private final PathfinderGPS plugin;

    public PathListCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pathfinder.list")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        if (plugin.getPathManager().getAllDestinationNames().isEmpty()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathlist.no_destinations"));
            return true;
        }

        // Si es un jugador, mostrar primero los del mismo mundo
        if (sender instanceof Player) {
            Player player = (Player) sender;
            displayDestinationsForPlayer(player);
        } else {
            displayAllDestinations(sender);
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("pathlist.footer"));

        return true;
    }

    private void displayDestinationsForPlayer(Player player) {
        String currentWorld = player.getWorld().getName();

        player.sendMessage(plugin.getLanguageManager().getMessage("pathlist.header"));

        // Primero mostrar destinos del mundo actual
        Set<String> currentWorldDests = plugin.getPathManager().getDestinationNamesInWorld(player.getWorld());
        if (!currentWorldDests.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathlist.current_world",
                    "world", currentWorld));
            for (String destName : currentWorldDests) {
                displayDestination(player, destName);
            }
        }

        // Luego mostrar destinos de otros mundos
        Set<String> allDests = plugin.getPathManager().getAllDestinationNames();
        Set<String> otherWorldDests = new HashSet<>(allDests);
        otherWorldDests.removeAll(currentWorldDests);

        if (!otherWorldDests.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(plugin.getLanguageManager().getMessage("pathlist.other_worlds"));
            for (String destName : otherWorldDests) {
                displayDestination(player, destName);
            }
        }
    }

    private void displayAllDestinations(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("pathlist.header"));

        for (String destName : plugin.getPathManager().getAllDestinationNames()) {
            displayDestination(sender, destName);
        }
    }

    private void displayDestination(CommandSender sender, String destName) {
        PathManager.PathDestination dest = plugin.getPathManager().getDestination(destName);
        Location loc = dest.getLocation();
        int routeCount = dest.getRouteCount();

        String pluralS = routeCount == 1 ? "" : "s";
        sender.sendMessage(plugin.getLanguageManager().getMessage("pathlist.entry",
                "name", destName, "routes", routeCount, "s", pluralS));
        sender.sendMessage(plugin.getLanguageManager().getMessage("pathlist.location",
                "world", loc.getWorld().getName(),
                "x", (int)loc.getX(),
                "y", (int)loc.getY(),
                "z", (int)loc.getZ()));
    }
}
