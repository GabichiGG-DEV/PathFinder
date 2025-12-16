package me.gabichigg.pathfinder;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final PathfinderGPS plugin;
    private FileConfiguration config;
    private File configFile;
    private final List<String> configErrors;

    // Valores por defecto
    private int maxWaypoints;
    private String waypointParticle;
    private String sessionLineParticle;
    private String navigationLineParticle;
    private int particleDensity;
    private String waypointSound;
    private float waypointSoundVolume;
    private float waypointSoundPitch;

    // Colores por distancia (RGB)
    private int[] colorClose;
    private int[] colorMedium;
    private int[] colorFar;
    private int[] colorVeryFar;

    // Distancias
    private double distanceClose;
    private double distanceMedium;
    private double distanceFar;

    public ConfigManager(PathfinderGPS plugin) {
        this.plugin = plugin;
        this.configErrors = new ArrayList<>();
        loadConfig();
    }

    public void loadConfig() {
        configErrors.clear();

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        try {
            // Waypoints
            maxWaypoints = getInt("waypoints.max_waypoints", 30, 1, 100);

            // Partículas
            waypointParticle = getString("particles.waypoint_particle", "DUST");
            sessionLineParticle = getString("particles.session_line_particle", "DUST");
            navigationLineParticle = getString("particles.navigation_line_particle", "DUST");
            particleDensity = getInt("particles.particle_density", 3, 1, 10);

            // Sonidos
            waypointSound = getString("sounds.waypoint_sound", "BLOCK_NOTE_BLOCK_HARP");
            waypointSoundVolume = (float) getDouble("sounds.waypoint_sound_volume", 1.0, 0.0, 2.0);
            waypointSoundPitch = (float) getDouble("sounds.waypoint_sound_pitch", 1.0, 0.5, 2.0);

            // Colores (RGB 0-255)
            colorClose = getColorArray("colors.distance_colors.close", new int[]{255, 85, 85});
            colorMedium = getColorArray("colors.distance_colors.medium", new int[]{255, 255, 85});
            colorFar = getColorArray("colors.distance_colors.far", new int[]{85, 255, 85});
            colorVeryFar = getColorArray("colors.distance_colors.very_far", new int[]{85, 85, 255});

            // Distancias
            distanceClose = getDouble("distances.close", 5.0, 0, 100);
            distanceMedium = getDouble("distances.medium", 15.0, 0, 100);
            distanceFar = getDouble("distances.far", 30.0, 0, 100);

            if (!configErrors.isEmpty()) {
                plugin.getLogger().warning("===== CONFIG ERRORS =====");
                for (String error : configErrors) {
                    plugin.getLogger().warning(error);
                }
                plugin.getLogger().warning("Using default values for invalid entries.");
            } else {
                plugin.getLogger().info("Configuration loaded successfully!");
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Critical error loading config.yml: " + e.getMessage());
            e.printStackTrace();
            loadDefaults();
        }
    }

    private void loadDefaults() {
        maxWaypoints = 30;
        waypointParticle = "DUST";
        sessionLineParticle = "DUST";
        navigationLineParticle = "DUST";
        particleDensity = 3;
        waypointSound = "BLOCK_NOTE_BLOCK_HARP";
        waypointSoundVolume = 1.0f;
        waypointSoundPitch = 1.0f;
        colorClose = new int[]{255, 85, 85};
        colorMedium = new int[]{255, 255, 85};
        colorFar = new int[]{85, 255, 85};
        colorVeryFar = new int[]{85, 85, 255};
        distanceClose = 5.0;
        distanceMedium = 15.0;
        distanceFar = 30.0;
    }

    private String getString(String path, String defaultValue) {
        if (!config.contains(path)) {
            configErrors.add("Missing config entry: " + path + " (using default: " + defaultValue + ")");
            return defaultValue;
        }
        return config.getString(path, defaultValue);
    }

    private int getInt(String path, int defaultValue, int min, int max) {
        if (!config.contains(path)) {
            configErrors.add("Missing config entry: " + path + " (using default: " + defaultValue + ")");
            return defaultValue;
        }

        int value = config.getInt(path, defaultValue);
        if (value < min || value > max) {
            configErrors.add("Invalid value at " + path + ": " + value + " (must be between " + min + " and " + max + ", using default: " + defaultValue + ")");
            return defaultValue;
        }
        return value;
    }

    private double getDouble(String path, double defaultValue, double min, double max) {
        if (!config.contains(path)) {
            configErrors.add("Missing config entry: " + path + " (using default: " + defaultValue + ")");
            return defaultValue;
        }

        double value = config.getDouble(path, defaultValue);
        if (value < min || value > max) {
            configErrors.add("Invalid value at " + path + ": " + value + " (must be between " + min + " and " + max + ", using default: " + defaultValue + ")");
            return defaultValue;
        }
        return value;
    }

    private int[] getColorArray(String path, int[] defaultValue) {
        if (!config.contains(path)) {
            configErrors.add("Missing config entry: " + path + " (using default RGB)");
            return defaultValue;
        }

        List<Integer> colorList = config.getIntegerList(path);
        if (colorList.size() != 3) {
            configErrors.add("Invalid color at " + path + ": must have 3 values [R, G, B] (using default)");
            return defaultValue;
        }

        int[] color = new int[3];
        for (int i = 0; i < 3; i++) {
            int value = colorList.get(i);
            if (value < 0 || value > 255) {
                configErrors.add("Invalid RGB value at " + path + "[" + i + "]: " + value + " (must be 0-255, using default)");
                return defaultValue;
            }
            color[i] = value;
        }
        return color;
    }

    // Getters
    public int getMaxWaypoints() { return maxWaypoints; }

    public Particle getWaypointParticle() {
        try {
            return Particle.valueOf(waypointParticle);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + waypointParticle + ", using DUST");
            return Particle.DUST;
        }
    }

    public Particle getSessionLineParticle() {
        try {
            return Particle.valueOf(sessionLineParticle);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + sessionLineParticle + ", using DUST");
            return Particle.DUST;
        }
    }

    public Particle getNavigationLineParticle() {
        try {
            return Particle.valueOf(navigationLineParticle);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + navigationLineParticle + ", using DUST");
            return Particle.DUST;
        }
    }

    public int getParticleDensity() { return particleDensity; }

    public Sound getWaypointSound() {
        try {
            return Sound.valueOf(waypointSound);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + waypointSound + ", using BLOCK_NOTE_BLOCK_HARP");
            return Sound.BLOCK_NOTE_BLOCK_HARP;
        }
    }

    public float getWaypointSoundVolume() { return waypointSoundVolume; }
    public float getWaypointSoundPitch() { return waypointSoundPitch; }

    // Métodos para verificar si la partícula soporta colores
    public boolean waypointParticleSupportsColor() {
        return supportsColor(getWaypointParticle());
    }

    public boolean sessionLineParticleSupportsColor() {
        return supportsColor(getSessionLineParticle());
    }

    public boolean navigationLineParticleSupportsColor() {
        return supportsColor(getNavigationLineParticle());
    }

    private boolean supportsColor(Particle particle) {
        return particle == Particle.DUST || particle == Particle.DUST_COLOR_TRANSITION;
    }

    public org.bukkit.Color getColorClose() { return org.bukkit.Color.fromRGB(colorClose[0], colorClose[1], colorClose[2]); }
    public org.bukkit.Color getColorMedium() { return org.bukkit.Color.fromRGB(colorMedium[0], colorMedium[1], colorMedium[2]); }
    public org.bukkit.Color getColorFar() { return org.bukkit.Color.fromRGB(colorFar[0], colorFar[1], colorFar[2]); }
    public org.bukkit.Color getColorVeryFar() { return org.bukkit.Color.fromRGB(colorVeryFar[0], colorVeryFar[1], colorVeryFar[2]); }

    public double getDistanceClose() { return distanceClose; }
    public double getDistanceMedium() { return distanceMedium; }
    public double getDistanceFar() { return distanceFar; }

    public boolean hasErrors() { return !configErrors.isEmpty(); }
    public List<String> getErrors() { return new ArrayList<>(configErrors); }
}