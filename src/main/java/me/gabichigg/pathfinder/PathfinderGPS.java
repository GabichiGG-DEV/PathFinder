package me.gabichigg.pathfinder;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PathfinderGPS extends JavaPlugin {

    private PathManager pathManager;
    private ParticleManager particleManager;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private NamespacedKey pathToolKey;
    private NamespacedKey waypointToolKey;

    @Override
    public void onEnable() {
        pathToolKey = new NamespacedKey(this, "path_tool");
        waypointToolKey = new NamespacedKey(this, "waypoint_tool");

        // Load configuration and language first
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);

        pathManager = new PathManager(this);
        particleManager = new ParticleManager(this);

        // Registrar el comando unificado
        getCommand("path").setExecutor(new PathCommand(this));

        getServer().getPluginManager().registerEvents(new PathToolListener(this), this);

        getLogger().info("PathfinderGPS activated successfully!");
    }

    @Override
    public void onDisable() {
        if (particleManager != null) {
            particleManager.stopAllPaths();
        }
        getLogger().info("PathfinderGPS deactivated!");
    }

    public PathManager getPathManager() {
        return pathManager;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public NamespacedKey getPathToolKey() {
        return pathToolKey;
    }

    public NamespacedKey getWaypointToolKey() {
        return waypointToolKey;
    }

    public ItemStack createPathTool() {
        ItemStack tool = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta meta = tool.getItemMeta();

        // Use language manager for name and lore
        String name = languageManager.getMessage("pathset.tool_name");
        java.util.List<String> lore = languageManager.getMessageList("pathset.tool_lore");

        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(name);
        }
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(pathToolKey, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        tool.setItemMeta(meta);
        return tool;
    }

    public ItemStack createWaypointTool(String pathName) {
        ItemStack tool = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = tool.getItemMeta();

        int maxWaypoints = configManager.getMaxWaypoints();

        // Use language manager for name and lore (with placeholders)
        String wpName = languageManager.getMessage("pathways.tool_name");
        java.util.List<String> wpLore = languageManager.getMessageList("pathways.tool_lore", "pathName", pathName, "max", maxWaypoints);

        if (wpName != null && !wpName.isEmpty()) {
            meta.setDisplayName(wpName);
        }
        if (wpLore != null && !wpLore.isEmpty()) {
            meta.setLore(wpLore);
        }

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(waypointToolKey, PersistentDataType.STRING, pathName);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        tool.setItemMeta(meta);
        return tool;
    }
}