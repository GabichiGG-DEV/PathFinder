package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParticleManager {

    private final PathfinderGPS plugin;
    private final Map<UUID, ActivePath> activePaths;
    private final Map<UUID, BukkitRunnable> routePreviews;

    public ParticleManager(PathfinderGPS plugin) {
        this.plugin = plugin;
        this.activePaths = new HashMap<>();
        this.routePreviews = new HashMap<>();
    }

    public void startPath(Player player, PathManager.Route route) {
        stopPath(player);

        ActivePath activePath = new ActivePath(route);
        activePaths.put(player.getUniqueId(), activePath);

        BukkitRunnable pathTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    activePaths.remove(player.getUniqueId());
                    return;
                }

                Location playerLoc = player.getLocation();

                // Verificar si llegó al destino
                if (playerLoc.distance(route.getDestination()) < 5) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("pathgo.arrived"));
                    this.cancel();
                    activePaths.remove(player.getUniqueId());
                    return;
                }

                // Mostrar camino
                displayRoute(player, route, playerLoc);
            }
        };

        pathTask.runTaskTimer(plugin, 0L, 10L);
        activePath.task = pathTask;
    }

    /**
     * Inicia navegación en línea recta (cuando no hay rutas creadas)
     */
    public void startDirectPath(Player player, Location destination) {
        stopPath(player);

        ActivePath activePath = new ActivePath(null);
        activePath.directDestination = destination;
        activePaths.put(player.getUniqueId(), activePath);

        BukkitRunnable pathTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    activePaths.remove(player.getUniqueId());
                    return;
                }

                Location playerLoc = player.getLocation();

                // Verificar si llegó al destino
                if (playerLoc.distance(destination) < 5) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("pathgo.arrived"));
                    this.cancel();
                    activePaths.remove(player.getUniqueId());
                    return;
                }

                // Mostrar línea recta
                displayDirectPath(player, playerLoc, destination);
            }
        };

        pathTask.runTaskTimer(plugin, 0L, 10L);
        activePath.task = pathTask;
    }

    /**
     * Inicia navegación en modo MIX (línea recta hasta primer waypoint, luego ruta)
     */
    public void startMixPath(Player player, PathManager.Route route) {
        stopPath(player);

        ActivePath activePath = new ActivePath(route);
        activePath.mixMode = true; // Activar modo MIX
        activePaths.put(player.getUniqueId(), activePath);

        BukkitRunnable pathTask = new BukkitRunnable() {
            private boolean reachedFirstWaypoint = false;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    activePaths.remove(player.getUniqueId());
                    return;
                }

                Location playerLoc = player.getLocation();
                Location firstWaypoint = route.getFirstWaypoint();

                // Verificar si llegó al destino final
                if (playerLoc.distance(route.getDestination()) < 5) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("pathgo.arrived"));
                    this.cancel();
                    activePaths.remove(player.getUniqueId());
                    return;
                }

                // Si no ha llegado al primer waypoint, mostrar línea recta
                if (!reachedFirstWaypoint) {
                    if (firstWaypoint != null && playerLoc.distance(firstWaypoint) < 3) {
                        reachedFirstWaypoint = true;
                        player.sendTitle("", plugin.getLanguageManager().getMessage("pathgo.mix_waypoint_reached"),
                                10, 40, 10);
                    } else {
                        displayDirectPath(player, playerLoc, firstWaypoint != null ? firstWaypoint : route.getDestination());
                        return;
                    }
                }

                // Una vez alcanzado el primer waypoint, mostrar ruta completa
                displayRoute(player, route, playerLoc);
            }
        };

        pathTask.runTaskTimer(plugin, 0L, 10L);
        activePath.task = pathTask;
    }

    private void displayRoute(Player player, PathManager.Route route, Location playerLoc) {
        List<Location> waypoints = route.getWaypoints();
        if (waypoints.isEmpty()) return;

        // Encontrar waypoint más cercano
        int closestIndex = findClosestWaypoint(waypoints, playerLoc);

        // Dibujar líneas desde el waypoint más cercano hasta el final
        int waypointsToShow = Math.min(15, waypoints.size() - closestIndex);

        for (int i = 0; i < waypointsToShow - 1; i++) {
            int index = closestIndex + i;
            if (index >= waypoints.size() - 1) break;

            Location start = waypoints.get(index);
            Location end = waypoints.get(index + 1);

            drawLine(player, start, end, playerLoc);
        }

        // Línea final hacia el destino
        if (closestIndex + waypointsToShow - 1 < waypoints.size()) {
            Location lastWaypoint = waypoints.get(Math.min(closestIndex + waypointsToShow - 1, waypoints.size() - 1));
            drawLine(player, lastWaypoint, route.getDestination(), playerLoc);
        }

        // Marcar waypoints visibles
        for (int i = 0; i < waypointsToShow; i++) {
            int index = closestIndex + i;
            if (index >= waypoints.size()) break;

            Location wp = waypoints.get(index);
            double dist = playerLoc.distance(wp);

            if (dist < 3) {
                player.spawnParticle(Particle.HAPPY_VILLAGER, wp.clone().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0);
            } else if (dist < 10) {
                player.spawnParticle(Particle.DUST, wp.clone().add(0, 0.5, 0), 2,
                        new Particle.DustOptions(org.bukkit.Color.AQUA, 1.2f));
            }
        }

        // Destino final
        Location destination = route.getDestination();
        player.spawnParticle(Particle.END_ROD, destination.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.02);
        player.spawnParticle(Particle.DUST, destination.clone().add(0, 0.2, 0), 10, 0.3, 0.1, 0.3, 0,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));

        // Distancia en action bar
        int distanceInt = (int) playerLoc.distance(destination);
        int nextWaypointIndex = closestIndex + 1;
        if (nextWaypointIndex < waypoints.size()) {
            int distToNext = (int) playerLoc.distance(waypoints.get(nextWaypointIndex));
            player.sendActionBar(plugin.getLanguageManager().getMessage("actionbar.navigation",
                    "distance", distanceInt,
                    "next", distToNext));
        } else {
            player.sendActionBar(plugin.getLanguageManager().getMessage("actionbar.navigation_final",
                    "distance", distanceInt));
        }
    }

    private int findClosestWaypoint(List<Location> waypoints, Location playerLoc) {
        int closestIndex = 0;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < waypoints.size(); i++) {
            double dist = playerLoc.distance(waypoints.get(i));
            if (dist < minDist) {
                minDist = dist;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    private void drawLine(Player player, Location start, Location end, Location playerLoc) {
        double distance = start.distance(end);
        double distanceFromPlayer = playerLoc.distance(start);

        int particles = (int) (distance / 0.3);
        if (particles < 2) particles = 2;
        if (particles > 50) particles = 50; // Límite para rendimiento

        double dx = (end.getX() - start.getX()) / particles;
        double dy = (end.getY() - start.getY()) / particles;
        double dz = (end.getZ() - start.getZ()) / particles;

        // Obtener configuración
        Particle navParticle = plugin.getConfigManager().getNavigationLineParticle();
        boolean supportsColor = plugin.getConfigManager().navigationLineParticleSupportsColor();

        double closeThreshold = plugin.getConfigManager().getDistanceClose();
        double mediumThreshold = plugin.getConfigManager().getDistanceMedium();
        double farThreshold = plugin.getConfigManager().getDistanceFar();

        for (int i = 0; i <= particles; i++) {
            double x = start.getX() + (dx * i);
            double y = start.getY() + (dy * i) + 0.5;
            double z = start.getZ() + (dz * i);

            Location particleLoc = new Location(start.getWorld(), x, y, z);

            if (supportsColor) {
                // Usar colores configurados si la partícula los soporta
                org.bukkit.Color color;
                if (distanceFromPlayer < closeThreshold) {
                    color = plugin.getConfigManager().getColorClose();
                } else if (distanceFromPlayer < mediumThreshold) {
                    color = plugin.getConfigManager().getColorMedium();
                } else if (distanceFromPlayer < farThreshold) {
                    color = plugin.getConfigManager().getColorFar();
                } else {
                    color = plugin.getConfigManager().getColorVeryFar();
                }
                player.spawnParticle(navParticle, particleLoc, 1,
                        new Particle.DustOptions(color, 1.0f));
            } else {
                // Para partículas sin color, simplemente mostrarlas
                player.spawnParticle(navParticle, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    public void stopPath(Player player) {
        ActivePath path = activePaths.remove(player.getUniqueId());
        if (path != null && path.task != null) {
            path.task.cancel();
        }
    }

    public void stopAllPaths() {
        for (ActivePath path : activePaths.values()) {
            if (path.task != null) {
                path.task.cancel();
            }
        }
        activePaths.clear();

        for (BukkitRunnable task : routePreviews.values()) {
            task.cancel();
        }
        routePreviews.clear();
    }

    public boolean hasActivePath(UUID playerId) {
        return activePaths.containsKey(playerId);
    }

    /**
     * Inicia visualización en tiempo real durante sesión de waypoints
     */
    public void startWaypointSessionVisuals(Player player, String destName) {
        stopRoutePreview(player); // Detener cualquier preview anterior

        BukkitRunnable visualTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    routePreviews.remove(player.getUniqueId());
                    return;
                }

                PathManager.WaypointSession session = plugin.getPathManager().getWaypointSession(player.getUniqueId());
                if (session == null) {
                    this.cancel();
                    routePreviews.remove(player.getUniqueId());
                    return;
                }

                showWaypointSessionPath(player, session);
            }
        };

        visualTask.runTaskTimer(plugin, 0L, 10L); // Cada 0.5 segundos
        routePreviews.put(player.getUniqueId(), visualTask);

        // Guardar referencia en la sesión
        PathManager.WaypointSession session = plugin.getPathManager().getWaypointSession(player.getUniqueId());
        if (session != null) {
            session.setVisualTask(visualTask);
        }
    }

    /**
     * Muestra el camino de la sesión de waypoints en tiempo real
     */
    private void showWaypointSessionPath(Player player, PathManager.WaypointSession session) {
        List<Location> waypoints = session.getWaypoints();

        if (waypoints.isEmpty()) {
            return;
        }

        // Dibujar líneas entre waypoints
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Location start = waypoints.get(i);
            Location end = waypoints.get(i + 1);
            drawSessionLine(player, start, end);
        }

        // Línea al destino final
        Location lastWaypoint = waypoints.get(waypoints.size() - 1);
        Location destination = session.getDestination();
        drawSessionLine(player, lastWaypoint, destination);

        // Marcar waypoints
        for (int i = 0; i < waypoints.size(); i++) {
            Location wp = waypoints.get(i);
            player.spawnParticle(Particle.DUST, wp.clone().add(0, 0.5, 0), 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(org.bukkit.Color.LIME, 1.2f));
        }

        // Marcar destino
        player.spawnParticle(Particle.END_ROD, destination.clone().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.01);
        player.spawnParticle(Particle.DUST, destination.clone().add(0, 0.2, 0), 5, 0.2, 0.1, 0.2, 0,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.3f));
    }

    /**
     * Dibuja línea para visualización de sesión
     */
    private void drawSessionLine(Player player, Location start, Location end) {
        double distance = start.distance(end);
        int particles = (int) (distance / 0.4);
        if (particles < 2) particles = 2;
        if (particles > 50) particles = 50;

        double dx = (end.getX() - start.getX()) / particles;
        double dy = (end.getY() - start.getY()) / particles;
        double dz = (end.getZ() - start.getZ()) / particles;

        Particle sessionParticle = plugin.getConfigManager().getSessionLineParticle();
        boolean supportsColor = plugin.getConfigManager().sessionLineParticleSupportsColor();

        for (int i = 0; i <= particles; i++) {
            double x = start.getX() + (dx * i);
            double y = start.getY() + (dy * i) + 0.5;
            double z = start.getZ() + (dz * i);

            Location particleLoc = new Location(start.getWorld(), x, y, z);

            if (supportsColor) {
                player.spawnParticle(sessionParticle, particleLoc, 1,
                        new Particle.DustOptions(org.bukkit.Color.AQUA, 0.9f));
            } else {
                player.spawnParticle(sessionParticle, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    /**
     * Inicia preview de una ruta guardada
     */
    public void startRoutePreview(Player player, PathManager.Route route) {
        stopRoutePreview(player); // Detener cualquier preview anterior

        BukkitRunnable previewTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    routePreviews.remove(player.getUniqueId());
                    return;
                }

                showRoutePreview(player, route);
            }
        };

        previewTask.runTaskTimer(plugin, 0L, 10L); // Cada 0.5 segundos
        routePreviews.put(player.getUniqueId(), previewTask);
    }

    /**
     * Muestra una ruta guardada completa
     */
    private void showRoutePreview(Player player, PathManager.Route route) {
        List<Location> waypoints = route.getWaypoints();

        // Dibujar todas las líneas
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Location start = waypoints.get(i);
            Location end = waypoints.get(i + 1);
            drawPreviewLine(player, start, end);
        }

        // Línea al destino
        if (!waypoints.isEmpty()) {
            Location lastWaypoint = waypoints.get(waypoints.size() - 1);
            drawPreviewLine(player, lastWaypoint, route.getDestination());
        }

        // Marcar waypoints
        for (Location wp : waypoints) {
            player.spawnParticle(Particle.DUST, wp.clone().add(0, 0.5, 0), 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.2f));
        }

        // Marcar destino
        Location destination = route.getDestination();
        player.spawnParticle(Particle.END_ROD, destination.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.02);
        player.spawnParticle(Particle.DUST, destination.clone().add(0, 0.2, 0), 8, 0.3, 0.1, 0.3, 0,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
    }

    /**
     * Dibuja línea para preview de ruta guardada
     */
    private void drawPreviewLine(Player player, Location start, Location end) {
        double distance = start.distance(end);
        int particles = (int) (distance / 0.3);
        if (particles < 2) particles = 2;
        if (particles > 50) particles = 50;

        double dx = (end.getX() - start.getX()) / particles;
        double dy = (end.getY() - start.getY()) / particles;
        double dz = (end.getZ() - start.getZ()) / particles;

        Particle navParticle = plugin.getConfigManager().getNavigationLineParticle();
        boolean supportsColor = plugin.getConfigManager().navigationLineParticleSupportsColor();

        for (int i = 0; i <= particles; i++) {
            double x = start.getX() + (dx * i);
            double y = start.getY() + (dy * i) + 0.5;
            double z = start.getZ() + (dz * i);

            Location particleLoc = new Location(start.getWorld(), x, y, z);

            if (supportsColor) {
                player.spawnParticle(navParticle, particleLoc, 1,
                        new Particle.DustOptions(org.bukkit.Color.FUCHSIA, 1.0f));
            } else {
                player.spawnParticle(navParticle, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    /**
     * Detiene la visualización de preview
     */
    public void stopRoutePreview(Player player) {
        BukkitRunnable task = routePreviews.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Muestra una línea recta hacia el destino
     */
    private void displayDirectPath(Player player, Location playerLoc, Location destination) {
        double distance = playerLoc.distance(destination);
        double maxDistance = Math.min(distance, 50); // Mostrar hasta 50 bloques

        // Calcular vector dirección
        double dx = destination.getX() - playerLoc.getX();
        double dy = destination.getY() - playerLoc.getY();
        double dz = destination.getZ() - playerLoc.getZ();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length == 0) return;

        dx /= length;
        dy /= length;
        dz /= length;

        // Obtener configuración
        Particle navParticle = plugin.getConfigManager().getNavigationLineParticle();
        boolean supportsColor = plugin.getConfigManager().navigationLineParticleSupportsColor();
        double closeThreshold = plugin.getConfigManager().getDistanceClose();
        double mediumThreshold = plugin.getConfigManager().getDistanceMedium();
        double farThreshold = plugin.getConfigManager().getDistanceFar();

        // Dibujar línea
        int particles = (int) (maxDistance / 0.5);
        for (int i = 0; i <= particles; i++) {
            double currentDist = i * 0.5;
            if (currentDist > maxDistance) break;

            double x = playerLoc.getX() + (dx * currentDist);
            double y = playerLoc.getY() + (dy * currentDist) + 0.5;
            double z = playerLoc.getZ() + (dz * currentDist);

            Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

            if (supportsColor) {
                // Colores según distancia desde el jugador
                org.bukkit.Color color;
                if (currentDist < closeThreshold) {
                    color = plugin.getConfigManager().getColorClose();
                } else if (currentDist < mediumThreshold) {
                    color = plugin.getConfigManager().getColorMedium();
                } else if (currentDist < farThreshold) {
                    color = plugin.getConfigManager().getColorFar();
                } else {
                    color = plugin.getConfigManager().getColorVeryFar();
                }
                player.spawnParticle(navParticle, particleLoc, 1,
                        new Particle.DustOptions(color, 1.0f));
            } else {
                // Sin colores, mostrar partícula normal
                player.spawnParticle(navParticle, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Destino final
        player.spawnParticle(Particle.END_ROD, destination.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.02);
        player.spawnParticle(Particle.DUST, destination.clone().add(0, 0.2, 0), 10, 0.3, 0.1, 0.3, 0,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));

        // Distancia en action bar
        int distanceInt = (int) distance;
        player.sendActionBar(plugin.getLanguageManager().getMessage("actionbar.direct_line",
                "distance", distanceInt));
    }

    private static class ActivePath {
        PathManager.Route route;
        Location directDestination; // Para modo línea recta
        boolean mixMode = false; // Para modo MIX
        BukkitRunnable task;

        ActivePath(PathManager.Route route) {
            this.route = route;
        }
    }
}
