package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PathToolListener implements Listener {

    private final PathfinderGPS plugin;
    private final Map<UUID, Long> lastClickTime;
    private static final long CLICK_COOLDOWN = 200; // 200ms entre clicks

    public PathToolListener(PathfinderGPS plugin) {
        this.plugin = plugin;
        this.lastClickTime = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Solo click derecho en bloque
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Seguridad: verificar que el bloque clicado no sea null (analizadores estáticos lo sugieren)
        if (event.getClickedBlock() == null) return;
        org.bukkit.block.Block clickedBlock = event.getClickedBlock();

        // Verificar herramienta de DESTINO (pala)
        if (item.getItemMeta().getPersistentDataContainer()
                .has(plugin.getPathToolKey(), PersistentDataType.BYTE)) {

            event.setCancelled(true);

            // Cooldown check
            if (!canClick(player)) {
                return;
            }

            handleDestinationTool(player, clickedBlock.getLocation().clone());
            return;
        }

        // Verificar herramienta de WAYPOINTS (brújula)
        if (item.getItemMeta().getPersistentDataContainer()
                .has(plugin.getWaypointToolKey(), PersistentDataType.STRING)) {

            event.setCancelled(true);

            // Cooldown check
            if (!canClick(player)) {
                return;
            }

            String destName = item.getItemMeta().getPersistentDataContainer()
                    .get(plugin.getWaypointToolKey(), PersistentDataType.STRING);
            // pass a clone to avoid mutating the block location
            handleWaypointTool(player, clickedBlock.getLocation().clone(), destName);
            return;
        }
    }

    private boolean canClick(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastClickTime.get(player.getUniqueId());

        if (last != null && (now - last) < CLICK_COOLDOWN) {
            return false; // Todavía en cooldown
        }

        lastClickTime.put(player.getUniqueId(), now);
        return true;
    }

    private void handleDestinationTool(Player player, Location clickedLocation) {
        if (!player.hasPermission("pathfinder.set")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return;
        }

        // Center the marker on top of the clicked block: add 0.5 to X and Z.
        Location destination = clickedLocation.add(0.5, 1, 0.5);
        plugin.getPathManager().setPendingDestination(player.getUniqueId(), destination);

        // Efectos
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        player.spawnParticle(Particle.END_ROD, destination, 30, 0.3, 0.5, 0.3, 0.05);

        player.sendMessage(plugin.getLanguageManager().getMessage("pathset.destination_marked"));
        player.sendMessage(plugin.getLanguageManager().getMessage("pathset.coordinates",
                "x", (int)destination.getX(),
                "y", (int)destination.getY(),
                "z", (int)destination.getZ()));
        player.sendMessage(plugin.getLanguageManager().getMessage("pathset.next_step"));
    }

    private void handleWaypointTool(Player player, Location clickedLocation, String destName) {
        if (!player.hasPermission("pathfinder.create")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return;
        }

        PathManager.WaypointSession session = plugin.getPathManager().getWaypointSession(player.getUniqueId());

        if (session == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("pathways.no_session"));
            return;
        }

        // Center the waypoint on the clicked block
        Location waypoint = clickedLocation.add(0.5, 1, 0.5);

        if (!plugin.getPathManager().addWaypoint(player.getUniqueId(), waypoint)) {
            int max = plugin.getConfigManager().getMaxWaypoints();
            player.sendMessage(plugin.getLanguageManager().getMessage("waypoint.limit_reached", "max", max));
            player.sendMessage(plugin.getLanguageManager().getMessage("waypoint.save_command", "destination", destName));
            return;
        }

        int count = session.getWaypointCount();
        int remaining = session.getRemainingSlots();
        int max = session.getMaxWaypoints();

        // Usar configuración para sonidos
        Sound waypointSound = plugin.getConfigManager().getWaypointSound();
        float volume = plugin.getConfigManager().getWaypointSoundVolume();
        float pitch = plugin.getConfigManager().getWaypointSoundPitch();

        // Obtener tipo de partícula configurada
        Particle waypointParticle = plugin.getConfigManager().getWaypointParticle();
        boolean supportsColor = plugin.getConfigManager().waypointParticleSupportsColor();

        // Efectos según número de waypoint
        if (supportsColor) {
            // Usar colores configurados si la partícula los soporta
            org.bukkit.Color particleColor;
            if (count <= 10) {
                particleColor = plugin.getConfigManager().getColorClose();
            } else if (count <= 20) {
                particleColor = plugin.getConfigManager().getColorMedium();
            } else {
                particleColor = plugin.getConfigManager().getColorFar();
            }

            player.spawnParticle(waypointParticle, waypoint, 30, 0.3, 0.3, 0.3, 0,
                    new Particle.DustOptions(particleColor, 1.8f));
        } else {
            // Para partículas sin color, usar sin opciones especiales
            player.spawnParticle(waypointParticle, waypoint, 30, 0.3, 0.3, 0.3, 0.05);
        }

        player.playSound(player.getLocation(), waypointSound, volume, pitch + (count * 0.1f));

        player.sendMessage(plugin.getLanguageManager().getMessage("waypoint.marked",
                "number", count, "remaining", remaining));

        if (remaining == 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage("waypoint.save_now"));
            player.sendMessage(plugin.getLanguageManager().getMessage("waypoint.save_command", "destination", destName));
        } else if (remaining <= 5) {
            player.sendMessage(plugin.getLanguageManager().getMessage("waypoint.limit_warning", "remaining", remaining));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getParticleManager().stopPath(event.getPlayer());
        plugin.getPathManager().removeWaypointSession(event.getPlayer().getUniqueId());
        lastClickTime.remove(event.getPlayer().getUniqueId());
    }
}