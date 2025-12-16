package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// /pathcreate <nombre> - Crea un destino base
class PathCreateCommand implements CommandExecutor {
    private final PathfinderGPS plugin;

    public PathCreateCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 1. Verificar si el remitente es un jugador
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_only"));
            return true;
        }

        Player player = (Player) sender;

        // 2. Verificar permisos
        if (!player.hasPermission("pathfinder.create")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        // 3. Verificar el uso (argumentos)
        if (args.length < 1) {
            // El mensaje ya incluye el formato correcto en el archivo de idioma
            player.sendMessage(plugin.getLanguageManager().getMessage("pathcreate.usage"));
            return true;
        }

        String destName = args[0];
        Location pendingDest = plugin.getPathManager().getPendingDestination(player.getUniqueId());

        // 4. Verificar si hay un destino pendiente marcado
        if (pendingDest == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathcreate.no_pending"));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathcreate.tool_help"));
            return true;
        }

        // 5. Intentar crear el destino
        if (plugin.getPathManager().createDestination(destName, pendingDest)) {
            // Éxito: Usa el sistema de placeholders del LanguageManager
            // Formato: getMessage(key, "placeholder1", value1, "placeholder2", value2, ...)
            player.sendMessage(plugin.getLanguageManager().getMessage("pathcreate.success",
                    "name", destName));
            player.sendMessage(plugin.getLanguageManager().getMessage("pathcreate.next_step",
                    "name", destName));

            plugin.getPathManager().removePendingDestination(player.getUniqueId());
        } else {
            // Error: Ya existe un destino con ese nombre
            player.sendMessage(plugin.getLanguageManager().getMessage("pathcreate.already_exists"));
        }

        return true;
    }
}