package me.gabichigg.pathfinder;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class LanguageManager {

    private final PathfinderGPS plugin;
    private String currentLanguage;
    private FileConfiguration langConfig;
    private final Map<String, String> messages;
    private final List<String> langErrors;
    private final Map<String, List<String>> messageLists;

    public LanguageManager(PathfinderGPS plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.messageLists = new HashMap<>();
        this.langErrors = new ArrayList<>();
        this.currentLanguage = "en";

        createDefaultLanguageFiles();
        loadLanguage(currentLanguage);
    }

    private void createDefaultLanguageFiles() {
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Crear archivos de idioma si no existen
        createLanguageFile("en.yml");
        createLanguageFile("es.yml");
        createLanguageFile("pt.yml");
        createLanguageFile("fr.yml");
        createLanguageFile("it.yml");
    }

    private void createLanguageFile(String fileName) {
        File langFile = new File(plugin.getDataFolder() + "/languages", fileName);
        if (!langFile.exists()) {
            plugin.saveResource("languages/" + fileName, false);
        }
    }

    public boolean loadLanguage(String lang) {
        langErrors.clear();
        messages.clear();
        messageLists.clear();

        File langFile = new File(plugin.getDataFolder() + "/languages", lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file not found: " + lang + ".yml");
            plugin.getLogger().warning("Available languages: " + String.join(", ", getAvailableLanguages()));
            return false;
        }

        try {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
            currentLanguage = lang;

            // Cargar todos los mensajes
            loadAllMessages();

            if (!langErrors.isEmpty()) {
                plugin.getLogger().warning("===== LANGUAGE FILE ERRORS (" + lang + ".yml) =====");
                for (String error : langErrors) {
                    plugin.getLogger().warning(error);
                }
                plugin.getLogger().warning("Using default messages for missing entries.");
            } else {
                plugin.getLogger().info("Language '" + lang + "' loaded successfully!");
            }

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Critical error loading " + lang + ".yml: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadAllMessages() {
        // Comandos generales
        loadMessage("commands.no_permission", "&cYou don't have permission to use this command.");
        loadMessage("commands.player_only", "&cThis command can only be used by players.");
        loadMessage("commands.player_not_found", "&cPlayer &e{player} &cis not online.");
        loadMessage("commands.invalid_usage", "&cInvalid usage. Use: &e{usage}");

        // PathSet
        // Name and lore for Destination tool (configurable)
        loadMessage("pathset.tool_name", "&6&lDestination Tool");
        loadMessageList("pathset.tool_lore", Arrays.asList(
                "&7Right-click to mark the &efinal destination",
                "&7Then use &e/path create <name>"
        ));

        loadMessage("pathset.tool_received", "&a&l✓ &aDestination tool received!");
        loadMessage("pathset.tool_usage", "&7Right-click to mark the &efinal destination&7.");
        loadMessage("pathset.destination_marked", "&a&l✓ &aFinal destination marked!");
        loadMessage("pathset.coordinates", "&7Coordinates: &e{x}, {y}, {z}");
        loadMessage("pathset.next_step", "&7Now use &e/path create <name> &7to create the destination.");

        // PathCreate
        loadMessage("pathcreate.usage", "&cUsage: /path create <name>");
        loadMessage("pathcreate.no_pending", "&cYou must first mark a destination with the tool.");
        loadMessage("pathcreate.tool_help", "&7Use &e/path set &7to get the tool.");
        loadMessage("pathcreate.success", "&a&l✓ &aDestination &e{name} &acreated successfully!");
        loadMessage("pathcreate.next_step", "&7Now use &e/path ways create {name} &7to create routes.");
        loadMessage("pathcreate.already_exists", "&cA destination with that name already exists.");

        // Pathways
        // Name and lore for Waypoint tool (configurable)
        loadMessage("pathways.tool_name", "&b&lWaypoint Tool");
        loadMessageList("pathways.tool_lore", Arrays.asList(
                "&7Creating route for: &e{pathName}",
                "&7Right-click to mark waypoints",
                "&7Maximum: &e{max} waypoints",
                "&7Undo: &e/path ways undo",
                "&7Save with: &e/path ways {pathName} save <name>"
        ));
        loadMessage("pathways.usage_create", "&cUsage: /path ways create <destination>");
        loadMessage("pathways.usage_save", "&cUsage: /path ways <destination> save <routeName>");
        loadMessage("pathways.usage_view", "&cUsage: /path ways <destination> view <routeName>");
        loadMessage("pathways.destination_not_found", "&cDestination &e{name} &cdoesn't exist.");
        loadMessage("pathways.see_list", "&7Use &e/path list &7to see available destinations.");
        loadMessage("pathways.tool_received", "&a&l✓ &aWaypoint tool received!");
        loadMessage("pathways.tool_usage", "&7Right-click to mark waypoints (max. {max})");
        loadMessage("pathways.undo_help", "&7Undo last: &e/path ways undo");
        loadMessage("pathways.save_help", "&7Save with: &e/path ways {destination} save <routeName>");
        loadMessage("pathways.route_saved", "&a&l✓ &aRoute &e{name} &asaved successfully!");
        loadMessage("pathways.now_navigate", "&7Now you can use &e/path go {destination}");
        loadMessage("pathways.no_session", "&cYou don't have an active waypoint session.");
        loadMessage("pathways.start_session", "&7Use &e/path ways create <destination> &7to start one.");
        loadMessage("pathways.route_not_found", "&cRoute &e{route} &cdoesn't exist for destination &e{destination}&c.");
        loadMessage("pathways.viewing_route", "&a&l✓ &aViewing route: &e{name}");
        loadMessage("pathways.waypoint_count", "&7Waypoints: &f{count}");
        loadMessage("pathways.stop_viewing", "&7Use &e/path ways stop &7to stop viewing.");
        loadMessage("pathways.viewing_stopped", "&a&l✓ &aViewing stopped.");
        loadMessage("pathways.undo_no_waypoints", "&cNo waypoints to undo.");
        loadMessage("pathways.undo_success", "&c&l✗ &cWaypoint removed. &7Remaining: &f{count}");
        loadMessage("pathways.undo_empty", "&7No more waypoints in the session.");

        // Waypoint Tool
        loadMessage("waypoint.marked", "&a&l✓ &aWaypoint #{number} marked! &7({remaining} remaining)");
        loadMessage("waypoint.limit_reached", "&c&lYou've reached the limit of {max} waypoints!");
        loadMessage("waypoint.limit_warning", "&7Only &e{remaining} &7waypoints available.");
        loadMessage("waypoint.save_now", "&e&l⚠ &eLimit reached! Save the route now:");
        loadMessage("waypoint.save_command", "&e/path ways {destination} save <name>");

        // PathGo
        loadMessage("pathgo.usage", "&cUsage: /path go <destination> [follow|nofollow|mix] [route] [player]");
        loadMessage("pathgo.no_admin_permission", "&cYou don't have permission to start navigation for other players.");
        loadMessage("pathgo.destination_not_found", "&cDestination &e{name} &cdoesn't exist.");
        loadMessage("pathgo.wrong_world", "&cThis destination is in another world.");
        loadMessage("pathgo.no_route_found", "&cCouldn't find a suitable route.");
        loadMessage("pathgo.started", "&a&l✓ &aNavigation started to &e{destination}&a!");
        loadMessage("pathgo.route_selected", "&7Route selected: &b{route}");
        loadMessage("pathgo.route_waypoints", "&7Waypoints: &f{count}");
        loadMessage("pathgo.started_by", "&7Started by: &f{player}");
        loadMessage("pathgo.started_for", "&a&l✓ &aNavigation started for &e{player} &ato &e{destination}&a!");
        loadMessage("pathgo.direct_mode", "&7Mode: &eDirect line &7(no route established)");
        loadMessage("pathgo.arrived", "&a&l✓ &aYou've arrived at your destination!");
        // PathGo - Modos adicionales
        loadMessage("pathgo.nofollow_mode", "&7Mode: &eDirect line &7(ignoring routes)");
        loadMessage("pathgo.mix_mode", "&7Mode: &eMix &7(direct to first waypoint of &b{route}&7)");
        loadMessage("pathgo.mix_waypoint_reached", "&a&l✓ &aFirst waypoint reached! Following route...");
        loadMessage("pathgo.route_not_found", "&cRoute &e{route} &cnot found for this destination.");

        // PathStop
        loadMessage("pathstop.stopped", "&a&l✓ &aNavigation stopped.");
        loadMessage("pathstop.no_active", "&cYou don't have any active navigation.");

        // PathList
        loadMessage("pathlist.no_destinations", "&cNo saved destinations.");
        loadMessage("pathlist.header", "&6&m                &r &6&lAVAILABLE DESTINATIONS &6&m                ");
        loadMessage("pathlist.entry", "&e▸ &f{name} &7({routes} route{s})");
        loadMessage("pathlist.location", "  &8└ &7{world} {x}, {y}, {z}");
        loadMessage("pathlist.footer", "&7Use &e/path go <name> &7to navigate.");

        // PathDelete
        loadMessage("pathdelete.usage", "&cUsage: /path delete <name>");
        loadMessage("pathdelete.success", "&a&l✓ &aDestination &e{name} &adeleted successfully!");
        loadMessage("pathdelete.not_found", "&cDestination &e{name} &cdoesn't exist.");

        //Multimundo
        loadMessage("pathlist.current_world", "&e&l▸ Current World: &f{world}");
        loadMessage("pathlist.other_worlds", "&e&l▸ Other Worlds:");

        // Action Bar Messages
        loadMessage("actionbar.navigation", "&e➤ &fDestination: &a{distance}m &7| Next waypoint: &e{next}m");
        loadMessage("actionbar.navigation_final", "&e➤ &fDestination: &a{distance}m");
        loadMessage("actionbar.direct_line", "&e➤ &fDirect line to destination: &a{distance}m");

        // Path Main Command (Help) - AGREGAR ESTO AL FINAL DE loadAllMessages()
        loadMessage("path.help_header", "&6&m                &r &6&lPATHFINDER GPS &6&m                ");
        loadMessage("path.help_set", "&e/path set &7- Get the destination tool");
        loadMessage("path.help_create", "&e/path create <n> &7- Create a destination");
        loadMessage("path.help_ways_create", "&e/path ways create <dest> &7- Start waypoint session");
        loadMessage("path.help_ways_save", "&e/path ways <dest> save <routeName> &7- Save route");
        loadMessage("path.help_ways_view", "&e/path ways <dest> view <routeName> &7- View route");
        loadMessage("path.help_ways_undo", "&e/path ways undo &7- Remove last waypoint");
        loadMessage("path.help_ways_stop", "&e/path ways stop &7- Stop viewing route");
        loadMessage("path.help_go", "&e/path go <dest> [mode] [route] &7- Navigate");
        loadMessage("path.help_go_modes", "&7  Modes: &bfollow&7, &bnofollow&7, &bmix");
        loadMessage("path.help_stop", "&e/path stop &7- Stop navigation");
        loadMessage("path.help_list", "&e/path list &7- List all destinations");
        loadMessage("path.help_delete", "&e/path delete <n> &7- Delete destination");
        loadMessage("path.help_admin_header", "&c&lAdmin Commands:");
        loadMessage("path.help_reload", "&e/path reload &7- Reload configuration");
        loadMessage("path.help_lang", "&e/path lang <language> &7- Change language");
        loadMessage("path.help_aliases", "&7Aliases: &e/pathfinder&7, &e/pf");
        loadMessage("path.help_footer", "&6&m                                        ");


        // Pathfinder main commands
        loadMessage("pathfinder.reload_success", "&a&l✓ &aConfiguration reloaded successfully!");
        loadMessage("pathfinder.reload_with_errors", "&e&l⚠ &eConfiguration reloaded with errors. Check console.");
        loadMessage("pathfinder.lang_changed", "&a&l✓ &aLanguage changed to: &e{lang}");
        loadMessage("pathfinder.lang_not_found", "&cLanguage file &e{lang}.yml &cnot found.");
        loadMessage("pathfinder.lang_available", "&7Available languages: &e{languages}");
        loadMessage("pathfinder.usage", "&e/pathfinder <reload|lang <language>>");
    }

    private void loadMessageList(String key, List<String> defaultLines) {
        String path = "messages." + key;
        if (!langConfig.contains(path) || !langConfig.isList(path)) {
            langErrors.add("Missing or invalid message list key: " + key);
            List<String> translated = new ArrayList<>();
            for (String line : defaultLines) {
                translated.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            messageLists.put(key, translated);
            return;
        }

        List<String> lines = langConfig.getStringList(path);
        List<String> translated = new ArrayList<>();
        for (String line : lines) {
            translated.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        messageLists.put(key, translated);
    }

    public List<String> getMessageList(String key, Object... replacements) {
        List<String> list = messageLists.get(key);
        if (list == null) {
            // Try to fallback to reading directly from config (in case loadAllMessages wasn't updated)
            String path = "messages." + key;
            if (langConfig != null && langConfig.contains(path) && langConfig.isList(path)) {
                List<String> lines = langConfig.getStringList(path);
                list = new ArrayList<>();
                for (String line : lines) {
                    list.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            } else {
                return Collections.emptyList();
            }
        }

        // Apply replacements
        List<String> result = new ArrayList<>();
        for (String line : list) {
            String replaced = line;
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    String placeholder = "{" + replacements[i] + "}";
                    String value = String.valueOf(replacements[i + 1]);
                    replaced = replaced.replace(placeholder, value);
                }
            }
            result.add(replaced);
        }

        return result;
    }

    private void loadMessage(String key, String defaultValue) {
        String path = "messages." + key;
        if (!langConfig.contains(path)) {
            langErrors.add("Missing message key: " + key);
            messages.put(key, ChatColor.translateAlternateColorCodes('&', defaultValue));
            return;
        }

        String value = langConfig.getString(path, defaultValue);
        messages.put(key, ChatColor.translateAlternateColorCodes('&', value));
    }

    public String getMessage(String key, Object... replacements) {
        String message = messages.getOrDefault(key, "&cMissing message: " + key);

        // Reemplazar placeholders
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String value = String.valueOf(replacements[i + 1]);
                message = message.replace(placeholder, value);
            }
        }

        return message;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        File langFolder = new File(plugin.getDataFolder(), "languages");

        if (langFolder.exists() && langFolder.isDirectory()) {
            File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    languages.add(file.getName().replace(".yml", ""));
                }
            }
        }

        return languages;
    }

    public boolean hasErrors() {
        return !langErrors.isEmpty();
    }

    public List<String> getErrors() {
        return new ArrayList<>(langErrors);
    }
}
