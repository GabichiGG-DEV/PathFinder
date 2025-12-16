package me.gabichigg.pathfinder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathfinderCommand implements CommandExecutor, TabCompleter {

    private final PathfinderGPS plugin;

    public PathfinderCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pathfinder.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);

            case "lang":
            case "language":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /pathfinder lang <language>");
                    List<String> available = plugin.getLanguageManager().getAvailableLanguages();
                    sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.lang_available",
                            "languages", String.join(", ", available)));
                    return true;
                }
                return handleLanguage(sender, args[1]);

            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.usage"));
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        sender.sendMessage("§7Reloading PathfinderGPS...");

        // Reload config
        plugin.getConfigManager().loadConfig();

        // Reload language
        String currentLang = plugin.getLanguageManager().getCurrentLanguage();
        plugin.getLanguageManager().loadLanguage(currentLang);

        // Check for errors
        boolean configErrors = plugin.getConfigManager().hasErrors();
        boolean langErrors = plugin.getLanguageManager().hasErrors();

        if (configErrors || langErrors) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.reload_with_errors"));

            if (configErrors) {
                sender.sendMessage("§e§lConfig Errors:");
                for (String error : plugin.getConfigManager().getErrors()) {
                    sender.sendMessage("§e- " + error);
                }
            }

            if (langErrors) {
                sender.sendMessage("§e§lLanguage Errors:");
                for (String error : plugin.getLanguageManager().getErrors()) {
                    sender.sendMessage("§e- " + error);
                }
            }

            sender.sendMessage("§eCheck console for more details.");
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.reload_success"));
        }

        return true;
    }

    private boolean handleLanguage(CommandSender sender, String lang) {
        // Remover .yml si el usuario lo incluye
        if (lang.endsWith(".yml")) {
            lang = lang.substring(0, lang.length() - 4);
        }

        boolean success = plugin.getLanguageManager().loadLanguage(lang);

        if (success) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.lang_changed", "lang", lang));

            if (plugin.getLanguageManager().hasErrors()) {
                sender.sendMessage("§e§lLanguage loaded with errors:");
                for (String error : plugin.getLanguageManager().getErrors()) {
                    sender.sendMessage("§e- " + error);
                }
            }
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.lang_not_found", "lang", lang));
            List<String> available = plugin.getLanguageManager().getAvailableLanguages();
            sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.lang_available",
                    "languages", String.join(", ", available)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("pathfinder.admin")) {
            return null;
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "lang");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("lang")) {
            return plugin.getLanguageManager().getAvailableLanguages();
        }

        return null;
    }
}