package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// /pathways - Gestiona waypoints para crear rutas O guarda/visualiza
class PathwaysCommand implements CommandExecutor, TabCompleter {
    private final PathfinderGPS plugin;

    public PathwaysCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("pathfinder.create")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_create"));
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.invalid_usage", "usage", "/pathways undo"));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_save"));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_view"));
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.invalid_usage", "usage", "/pathways stop"));
            return true;
        }

        String firstArg = args[0];

        // Comando: /pathways undo
        if (firstArg.equalsIgnoreCase("undo")) {
            return handleUndo(player);
        }

        // Comando: /pathways stop
        if (firstArg.equalsIgnoreCase("stop")) {
            plugin.getParticleManager().stopRoutePreview(player);
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.viewing_stopped"));
            return true;
        }

        // Comando: /pathways create <destino>
        if (firstArg.equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_create"));
                return true;
            }

            String destName = args[1];

            if (!plugin.getPathManager().destinationExists(destName)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.destination_not_found", "name", destName));
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.see_list"));
                return true;
            }

            // Iniciar sesión de waypoints
            plugin.getPathManager().startWaypointSession(player.getUniqueId(), destName);
            player.getInventory().addItem(plugin.createWaypointTool(destName));

            // Iniciar visualización en tiempo real
            plugin.getParticleManager().startWaypointSessionVisuals(player, destName);

            int max = plugin.getConfigManager().getMaxWaypoints();
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.tool_received"));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.tool_usage", "max", max));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.undo_help"));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.save_help", "destination", destName));

            return true;
        }

        // Los demás comandos usan el destino como primer argumento
        String destName = firstArg;

        if (!plugin.getPathManager().destinationExists(destName)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.destination_not_found", "name", destName));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.see_list"));
            return true;
        }

        // Comando: /pathways <destino> save <nombreRuta>
        if (args.length >= 3 && args[1].equalsIgnoreCase("save")) {
            String routeName = args[2];

            if (plugin.getPathManager().saveRoute(player.getUniqueId(), routeName)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.route_saved", "name", routeName));
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.now_navigate", "destination", destName));
            } else {
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.no_session"));
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.start_session"));
            }
            return true;
        }

        // Comando: /pathways <destino> view <nombreRuta>
        if (args.length >= 3 && args[1].equalsIgnoreCase("view")) {
            String routeName = args[2];

            PathManager.PathDestination dest = plugin.getPathManager().getDestination(destName);
            PathManager.Route route = null;

            // Buscar la ruta
            for (PathManager.Route r : dest.getRoutes()) {
                if (r.getName().equalsIgnoreCase(routeName)) {
                    route = r;
                    break;
                }
            }

            if (route == null) {
                player.sendMessage(plugin.getLanguageManager().getMessage("pathways.route_not_found",
                        "route", routeName, "destination", destName));
                return true;
            }

            plugin.getParticleManager().startRoutePreview(player, route);
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.viewing_route", "name", routeName));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.waypoint_count", "count", route.getWaypoints().size()));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.stop_viewing"));
            return true;
        }

        player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_create"));
        player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_save"));
        player.sendMessage(plugin.getLanguageManager().getMessage("pathways.usage_view"));

        return true;
    }

    private boolean handleUndo(Player player) {
        PathManager.WaypointSession session = plugin.getPathManager().getWaypointSession(player.getUniqueId());

        if (session == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.no_session"));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.start_session"));
            return true;
        }

        if (session.getWaypoints().isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.undo_no_waypoints"));
            return true;
        }

        // Eliminar último waypoint
        Location removed = session.removeLastWaypoint();
        int remaining = session.getWaypointCount();

        // Efecto visual en el waypoint eliminado
        player.spawnParticle(Particle.SMOKE, removed, 30, 0.3, 0.3, 0.3, 0.05);
        player.spawnParticle(Particle.DUST, removed, 20, 0.3, 0.3, 0.3, 0,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);

        player.sendMessage(plugin.getLanguageManager().getMessage("pathways.undo_success", "count", remaining));

        if (remaining == 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.undo_empty"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("create");
            suggestions.add("undo");
            suggestions.add("stop");
            suggestions.addAll(plugin.getPathManager().getAllDestinationNames());
            return suggestions;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                return new ArrayList<>(plugin.getPathManager().getAllDestinationNames());
            } else if (!args[0].equalsIgnoreCase("undo") && !args[0].equalsIgnoreCase("stop")) {
                return Arrays.asList("save", "view");
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("view")) {
            // Tab complete para nombres de rutas
            PathManager.PathDestination dest = plugin.getPathManager().getDestination(args[0]);
            if (dest != null) {
                List<String> routeNames = new ArrayList<>();
                for (PathManager.Route route : dest.getRoutes()) {
                    routeNames.add(route.getName());
                }
                return routeNames;
            }
        }
        return null;
    }
}
