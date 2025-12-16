package me.gabichigg.pathfinder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// /pathstop - Detiene la navegación activa
class PathStopCommand implements CommandExecutor {
    private final PathfinderGPS plugin;

    public PathStopCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("pathfinder.use")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        if (plugin.getParticleManager().hasActivePath(player.getUniqueId())) {
            plugin.getParticleManager().stopPath(player);
            player.sendMessage(plugin.getLanguageManager().getMessage("pathstop.stopped"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathstop.no_active"));
        }

        return true;
    }
}