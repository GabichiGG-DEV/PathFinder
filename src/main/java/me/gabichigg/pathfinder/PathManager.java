package me.gabichigg.pathfinder;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PathManager {

    private final PathfinderGPS plugin;
    private final Map<UUID, Location> pendingDestinations; // Destino marcado con pala
    private final Map<UUID, WaypointSession> waypointSessions; // Sesiones de marcado de waypoints
    private final Map<String, PathDestination> destinations; // Destinos con sus rutas
    private final File pathsFile;
    private FileConfiguration pathsConfig;

    public PathManager(PathfinderGPS plugin) {
        this.plugin = plugin;
        this.pendingDestinations = new HashMap<>();
        this.waypointSessions = new HashMap<>();
        this.destinations = new HashMap<>();
        this.pathsFile = new File(plugin.getDataFolder(), "paths.yml");
        loadPaths();
    }

    // === MANEJO DE DESTINOS ===

    public void setPendingDestination(UUID playerId, Location location) {
        pendingDestinations.put(playerId, location);
    }

    public Location getPendingDestination(UUID playerId) {
        return pendingDestinations.get(playerId);
    }

    public void removePendingDestination(UUID playerId) {
        pendingDestinations.remove(playerId);
    }

    // === CREACIÓN DE DESTINOS ===

    public boolean createDestination(String name, Location destination) {
        if (destinations.containsKey(name.toLowerCase())) {
            return false;
        }

        PathDestination pathDest = new PathDestination(name, destination);
        destinations.put(name.toLowerCase(), pathDest);
        saveDestination(pathDest);
        return true;
    }

    public PathDestination getDestination(String name) {
        return destinations.get(name.toLowerCase());
    }

    public boolean destinationExists(String name) {
        return destinations.containsKey(name.toLowerCase());
    }

    public Set<String> getAllDestinationNames() {
        return destinations.keySet();
    }

    /**
     * Obtiene nombres de destinos en un mundo específico
     */
    public Set<String> getDestinationNamesInWorld(org.bukkit.World world) {
        Set<String> worldDestinations = new HashSet<>();
        for (Map.Entry<String, PathDestination> entry : destinations.entrySet()) {
            if (entry.getValue().getLocation().getWorld().getName().equals(world.getName())) {
                worldDestinations.add(entry.getKey());
            }
        }
        return worldDestinations;
    }

    /**
     * Verifica si un destino está en un mundo específico
     */
    public boolean isDestinationInWorld(String destName, org.bukkit.World world) {
        PathDestination dest = getDestination(destName);
        if (dest == null) return false;
        return dest.getLocation().getWorld().getName().equals(world.getName());
    }

    public boolean deleteDestination(String name) {
        PathDestination dest = destinations.remove(name.toLowerCase());
        if (dest == null) return false;

        pathsConfig.set("destinations." + name.toLowerCase(), null);
        try {
            pathsConfig.save(pathsFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // === SESIONES DE WAYPOINTS ===

    public void startWaypointSession(UUID playerId, String destinationName) {
        PathDestination dest = getDestination(destinationName);
        if (dest == null) return;

        WaypointSession session = new WaypointSession(plugin, destinationName, dest.getLocation());
        waypointSessions.put(playerId, session);
    }

    public WaypointSession getWaypointSession(UUID playerId) {
        return waypointSessions.get(playerId);
    }

    public void removeWaypointSession(UUID playerId) {
        WaypointSession session = waypointSessions.remove(playerId);
        if (session != null) {
            session.stopVisualTask();
        }
    }

    public boolean addWaypoint(UUID playerId, Location location) {
        WaypointSession session = waypointSessions.get(playerId);
        if (session == null) return false;

        return session.addWaypoint(location);
    }

    public boolean saveRoute(UUID playerId, String routeName) {
        WaypointSession session = waypointSessions.get(playerId);
        if (session == null) return false;

        PathDestination dest = getDestination(session.getDestinationName());
        if (dest == null) return false;

        Route route = new Route(routeName, session.getWaypoints(), session.getDestination());
        dest.addRoute(route);

        saveDestination(dest);
        waypointSessions.remove(playerId);
        return true;
    }

    // === BÚSQUEDA DE MEJOR RUTA ===

    public Route findBestRoute(String destinationName, Location playerLocation) {
        PathDestination dest = getDestination(destinationName);
        if (dest == null) return null;

        List<Route> routes = dest.getRoutes();
        if (routes.isEmpty()) return null;

        // Encontrar la ruta cuyo primer waypoint esté más cerca
        Route bestRoute = null;
        double minDistance = Double.MAX_VALUE;

        for (Route route : routes) {
            Location firstWaypoint = route.getFirstWaypoint();
            if (firstWaypoint == null) continue;

            double distance = playerLocation.distance(firstWaypoint);
            if (distance < minDistance) {
                minDistance = distance;
                bestRoute = route;
            }
        }

        return bestRoute;
    }

    // === GUARDADO Y CARGA ===

    private void saveDestination(PathDestination dest) {
        String path = "destinations." + dest.getName().toLowerCase();

        pathsConfig.set(path + ".world", dest.getLocation().getWorld().getName());
        pathsConfig.set(path + ".x", dest.getLocation().getX());
        pathsConfig.set(path + ".y", dest.getLocation().getY());
        pathsConfig.set(path + ".z", dest.getLocation().getZ());

        // Guardar rutas
        List<Map<String, Object>> routesList = new ArrayList<>();
        for (Route route : dest.getRoutes()) {
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("name", route.getName());

            List<Map<String, Object>> waypointsList = new ArrayList<>();
            for (Location loc : route.getWaypoints()) {
                Map<String, Object> wpData = new HashMap<>();
                wpData.put("x", loc.getX());
                wpData.put("y", loc.getY());
                wpData.put("z", loc.getZ());
                waypointsList.add(wpData);
            }
            routeData.put("waypoints", waypointsList);
            routesList.add(routeData);
        }

        pathsConfig.set(path + ".routes", routesList);

        try {
            pathsConfig.save(pathsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPaths() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        if (!pathsFile.exists()) {
            try {
                pathsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pathsConfig = YamlConfiguration.loadConfiguration(pathsFile);

        if (!pathsConfig.contains("destinations")) return;

        ConfigurationSection destsSection = pathsConfig.getConfigurationSection("destinations");
        for (String destName : destsSection.getKeys(false)) {
            String path = "destinations." + destName;

            String worldName = pathsConfig.getString(path + ".world");
            double x = pathsConfig.getDouble(path + ".x");
            double y = pathsConfig.getDouble(path + ".y");
            double z = pathsConfig.getDouble(path + ".z");

            // Verificar que el mundo existe antes de cargar
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Destination '" + destName + "' references non-existent world: " + worldName);
                continue;
            }

            Location destLoc = new Location(world, x, y, z);
            PathDestination dest = new PathDestination(destName, destLoc);

            // Cargar rutas
            if (pathsConfig.contains(path + ".routes")) {
                List<Map<?, ?>> routesList = pathsConfig.getMapList(path + ".routes");
                for (Map<?, ?> routeData : routesList) {
                    String routeName = (String) routeData.get("name");
                    List<Map<?, ?>> waypointsList = (List<Map<?, ?>>) routeData.get("waypoints");

                    List<Location> waypoints = new ArrayList<>();
                    for (Map<?, ?> wpData : waypointsList) {
                        double wpX = ((Number) wpData.get("x")).doubleValue();
                        double wpY = ((Number) wpData.get("y")).doubleValue();
                        double wpZ = ((Number) wpData.get("z")).doubleValue();
                        waypoints.add(new Location(world, wpX, wpY, wpZ));
                    }

                    Route route = new Route(routeName, waypoints, destLoc);
                    dest.addRoute(route);
                }
            }

            destinations.put(destName.toLowerCase(), dest);
        }

        plugin.getLogger().info("Loaded " + destinations.size() + " destinations");
    }

    // === CLASES INTERNAS ===

    public static class PathDestination {
        private final String name;
        private final Location location;
        private final List<Route> routes;

        public PathDestination(String name, Location location) {
            this.name = name;
            this.location = location;
            this.routes = new ArrayList<>();
        }

        public String getName() { return name; }
        public Location getLocation() { return location; }
        public List<Route> getRoutes() { return routes; }

        public void addRoute(Route route) {
            // Eliminar ruta con el mismo nombre si existe
            routes.removeIf(r -> r.getName().equalsIgnoreCase(route.getName()));
            routes.add(route);
        }

        public int getRouteCount() { return routes.size(); }
    }

    public static class Route {
        private final String name;
        private final List<Location> waypoints;
        private final Location destination;

        public Route(String name, List<Location> waypoints, Location destination) {
            this.name = name;
            this.waypoints = new ArrayList<>(waypoints);
            this.destination = destination;
        }

        public String getName() { return name; }
        public List<Location> getWaypoints() { return waypoints; }
        public Location getDestination() { return destination; }

        public Location getFirstWaypoint() {
            return waypoints.isEmpty() ? null : waypoints.get(0);
        }
    }

    public static class WaypointSession {
        private final PathfinderGPS plugin;
        private final String destinationName;
        private final Location destination;
        private final List<Location> waypoints;
        private final int maxWaypoints;
        private BukkitRunnable visualTask;

        public WaypointSession(PathfinderGPS plugin, String destinationName, Location destination) {
            this.plugin = plugin;
            this.destinationName = destinationName;
            this.destination = destination;
            this.waypoints = new ArrayList<>();
            this.maxWaypoints = plugin.getConfigManager().getMaxWaypoints();
        }

        public boolean addWaypoint(Location location) {
            if (waypoints.size() >= maxWaypoints) {
                return false;
            }
            waypoints.add(location);
            return true;
        }

        public Location removeLastWaypoint() {
            if (waypoints.isEmpty()) {
                return null;
            }
            return waypoints.remove(waypoints.size() - 1);
        }

        public String getDestinationName() { return destinationName; }
        public Location getDestination() { return destination; }
        public List<Location> getWaypoints() { return waypoints; }
        public int getWaypointCount() { return waypoints.size(); }
        public int getRemainingSlots() { return maxWaypoints - waypoints.size(); }
        public int getMaxWaypoints() { return maxWaypoints; }

        public void setVisualTask(BukkitRunnable task) {
            this.visualTask = task;
        }

        public void stopVisualTask() {
            if (visualTask != null) {
                visualTask.cancel();
                visualTask = null;
            }
        }
    }
}