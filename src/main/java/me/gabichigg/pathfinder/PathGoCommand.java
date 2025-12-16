package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// /pathgo <destino> [follow|nofollow|mix] [ruta] [jugador]
class PathGoCommand implements CommandExecutor, TabCompleter {
    private final PathfinderGPS plugin;

    public PathGoCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.usage"));
            return true;
        }

        String destName = args[0];
        String mode = "follow"; // Modo por defecto
        String specificRoute = null;
        Player targetPlayer = null;

        // Analizar argumentos
        int currentArg = 1;

        // Verificar si hay modo especificado
        if (args.length > currentArg) {
            String possibleMode = args[currentArg].toLowerCase();
            if (possibleMode.equals("follow") || possibleMode.equals("nofollow") || possibleMode.equals("mix")) {
                mode = possibleMode;
                currentArg++;
            }
        }

        // Si el modo es "follow", verificar si hay nombre de ruta específica
        if (mode.equals("follow") && args.length > currentArg) {
            // Verificar si el siguiente argumento es un jugador o un nombre de ruta
            String nextArg = args[currentArg];
            Player possiblePlayer = plugin.getServer().getPlayer(nextArg);

            // Si no es un jugador válido o no es %player%, entonces es un nombre de ruta
            if (possiblePlayer == null && !nextArg.equals("%player%")) {
                specificRoute = nextArg;
                currentArg++;
            }
        }

        // Verificar si hay jugador especificado
        if (args.length > currentArg) {
            if (!sender.hasPermission("pathfinder.admin")) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.no_admin_permission"));
                return true;
            }

            String playerName = args[currentArg];
            if (playerName.equals("%player%") && sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                targetPlayer = plugin.getServer().getPlayer(playerName);
            }

            if (targetPlayer == null) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_not_found",
                        "player", playerName));
                return true;
            }
        } else {
            // Modo normal: comando ejecutado por el jugador para sí mismo
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.usage"));
                return true;
            }

            if (!sender.hasPermission("pathfinder.use")) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
                return true;
            }

            targetPlayer = (Player) sender;
        }

        // Verificar que el destino existe
        PathManager.PathDestination dest = plugin.getPathManager().getDestination(destName);

        if (dest == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.destination_not_found",
                    "name", destName));
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathways.see_list"));
            return true;
        }

        // Verificar que el mundo del destino existe
        if (dest.getLocation().getWorld() == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.wrong_world"));
            plugin.getLogger().warning("Destination '" + destName + "' has invalid world!");
            return true;
        }

        // Verificar que el jugador está en el mismo mundo
        if (!targetPlayer.getWorld().getName().equals(dest.getLocation().getWorld().getName())) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.wrong_world"));
            return true;
        }

        // Ejecutar según el modo seleccionado
        boolean success = false;
        switch (mode) {
            case "nofollow":
                success = handleNoFollow(sender, targetPlayer, dest, destName);
                break;
            case "mix":
                success = handleMix(sender, targetPlayer, dest, destName);
                break;
            case "follow":
            default:
                success = handleFollow(sender, targetPlayer, dest, destName, specificRoute);
                break;
        }

        return success;
    }

    /**
     * Modo FOLLOW: Sigue una ruta específica o la más cercana
     */
    private boolean handleFollow(CommandSender sender, Player targetPlayer, PathManager.PathDestination dest,
                                 String destName, String specificRoute) {

        // Si no hay rutas, usar línea recta
        if (dest.getRouteCount() == 0) {
            plugin.getParticleManager().startDirectPath(targetPlayer, dest.getLocation());
            sendNavigationMessages(sender, targetPlayer, destName, null, "direct");
            return true;
        }

        PathManager.Route selectedRoute = null;

        // Si se especificó una ruta, buscarla
        if (specificRoute != null) {
            for (PathManager.Route route : dest.getRoutes()) {
                if (route.getName().equalsIgnoreCase(specificRoute)) {
                    selectedRoute = route;
                    break;
                }
            }

            if (selectedRoute == null) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.route_not_found",
                        "route", specificRoute));
                return true;
            }
        } else {
            // Buscar la ruta más cercana
            selectedRoute = plugin.getPathManager().findBestRoute(destName, targetPlayer.getLocation());

            if (selectedRoute == null) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.no_route_found"));
                return true;
            }
        }

        // Iniciar navegación con la ruta seleccionada
        plugin.getParticleManager().startPath(targetPlayer, selectedRoute);
        sendNavigationMessages(sender, targetPlayer, destName, selectedRoute, "follow");

        return true;
    }

    /**
     * Modo NOFOLLOW: Línea recta sin importar si hay rutas
     */
    private boolean handleNoFollow(CommandSender sender, Player targetPlayer, PathManager.PathDestination dest,
                                   String destName) {
        plugin.getParticleManager().startDirectPath(targetPlayer, dest.getLocation());
        sendNavigationMessages(sender, targetPlayer, destName, null, "nofollow");
        return true;
    }

    /**
     * Modo MIX: Línea recta hasta el primer waypoint, luego sigue la ruta
     */
    private boolean handleMix(CommandSender sender, Player targetPlayer, PathManager.PathDestination dest,
                              String destName) {

        // Si no hay rutas, usar línea recta normal
        if (dest.getRouteCount() == 0) {
            plugin.getParticleManager().startDirectPath(targetPlayer, dest.getLocation());
            sendNavigationMessages(sender, targetPlayer, destName, null, "direct");
            return true;
        }

        // Buscar la mejor ruta
        PathManager.Route bestRoute = plugin.getPathManager().findBestRoute(destName, targetPlayer.getLocation());

        if (bestRoute == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.no_route_found"));
            return true;
        }

        // Iniciar navegación en modo MIX
        plugin.getParticleManager().startMixPath(targetPlayer, bestRoute);
        sendNavigationMessages(sender, targetPlayer, destName, bestRoute, "mix");

        return true;
    }

    /**
     * Envía los mensajes de navegación apropiados
     */
    private void sendNavigationMessages(CommandSender sender, Player targetPlayer, String destName,
                                        PathManager.Route route, String mode) {
        if (sender.equals(targetPlayer)) {
            // El jugador inició su propia navegación
            targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.started",
                    "destination", destName));

            switch (mode) {
                case "follow":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.route_selected",
                            "route", route.getName()));
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.route_waypoints",
                            "count", route.getWaypoints().size()));
                    break;
                case "nofollow":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.nofollow_mode"));
                    break;
                case "mix":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.mix_mode",
                            "route", route.getName()));
                    break;
                case "direct":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.direct_mode"));
                    break;
            }
        } else {
            // Alguien más inició la navegación
            targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.started",
                    "destination", destName));

            switch (mode) {
                case "follow":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.route_selected",
                            "route", route.getName()));
                    break;
                case "nofollow":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.nofollow_mode"));
                    break;
                case "mix":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.mix_mode",
                            "route", route.getName()));
                    break;
                case "direct":
                    targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.direct_mode"));
                    break;
            }

            targetPlayer.sendMessage(plugin.getLanguageManager().getMessage("pathgo.started_by",
                    "player", sender.getName()));

            sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.started_for",
                    "player", targetPlayer.getName(), "destination", destName));

            if (route != null) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("pathgo.route_selected",
                        "route", route.getName()));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Nombres de destinos
            suggestions.addAll(plugin.getPathManager().getAllDestinationNames());
        } else if (args.length == 2) {
            // Modos: follow, nofollow, mix
            suggestions.addAll(Arrays.asList("follow", "nofollow", "mix"));

            // También incluir jugadores si tiene permiso admin
            if (sender.hasPermission("pathfinder.admin")) {
                suggestions.add("%player%");
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    suggestions.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            // Si el modo es "follow", mostrar rutas disponibles
            if (args[1].equalsIgnoreCase("follow")) {
                PathManager.PathDestination dest = plugin.getPathManager().getDestination(args[0]);
                if (dest != null) {
                    for (PathManager.Route route : dest.getRoutes()) {
                        suggestions.add(route.getName());
                    }
                }
            }

            // También jugadores si tiene permiso
            if (sender.hasPermission("pathfinder.admin")) {
                suggestions.add("%player%");
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    suggestions.add(p.getName());
                }
            }
        } else if (args.length == 4 && sender.hasPermission("pathfinder.admin")) {
            // Jugadores en la última posición
            suggestions.add("%player%");
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                suggestions.add(p.getName());
            }
        }

        return suggestions;
    }
}