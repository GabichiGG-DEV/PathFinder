package me.gabichigg.pathfinder;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathCommand implements CommandExecutor, TabCompleter {

    private final PathfinderGPS plugin;
    private final PathSetCommand pathSetCommand;
    private final PathCreateCommand pathCreateCommand;
    private final PathwaysCommand pathwaysCommand;
    private final PathGoCommand pathGoCommand;
    private final PathStopCommand pathStopCommand;
    private final PathListCommand pathListCommand;
    private final PathDeleteCommand pathDeleteCommand;

    public PathCommand(PathfinderGPS plugin) {
        this.plugin = plugin;
        this.pathSetCommand = new PathSetCommand(plugin);
        this.pathCreateCommand = new PathCreateCommand(plugin);
        this.pathwaysCommand = new PathwaysCommand(plugin);
        this.pathGoCommand = new PathGoCommand(plugin);
        this.pathStopCommand = new PathStopCommand(plugin);
        this.pathListCommand = new PathListCommand(plugin);
        this.pathDeleteCommand = new PathDeleteCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "set":
                return pathSetCommand.onCommand(sender, command, label, subArgs);

            case "create":
                return pathCreateCommand.onCommand(sender, command, label, subArgs);

            case "ways":
            case "waypoints":
                return pathwaysCommand.onCommand(sender, command, label, subArgs);

            case "go":
            case "navigate":
                return pathGoCommand.onCommand(sender, command, label, subArgs);

            case "stop":
                return pathStopCommand.onCommand(sender, command, label, subArgs);

            case "list":
                return pathListCommand.onCommand(sender, command, label, subArgs);

            case "delete":
            case "remove":
                return pathDeleteCommand.onCommand(sender, command, label, subArgs);

            case "reload":
                return handleReload(sender);

            case "lang":
            case "language":
                if (subArgs.length < 1) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.usage"));
                    List<String> available = plugin.getLanguageManager().getAvailableLanguages();
                    sender.sendMessage(plugin.getLanguageManager().getMessage("pathfinder.lang_available",
                            "languages", String.join(", ", available)));
                    return true;
                }
                return handleLanguage(sender, subArgs[0]);

            case "help":
            case "?":
                sendHelp(sender);
                return true;

            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.invalid_usage",
                        "usage", "/path help"));
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_header"));
        sender.sendMessage("");
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_set"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_create"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_ways_create"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_ways_save"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_ways_view"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_ways_undo"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_ways_stop"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_go"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_go_modes"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_stop"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_list"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_delete"));

        if (sender.hasPermission("pathfinder.admin")) {
            sender.sendMessage("");
            sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_admin_header"));
            sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_reload"));
            sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_lang"));
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_aliases"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("path.help_footer"));
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("pathfinder.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

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
        if (!sender.hasPermission("pathfinder.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }

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
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Subcomandos principales
            suggestions.addAll(Arrays.asList("set", "create", "ways", "go", "stop", "list", "delete", "help"));

            if (sender.hasPermission("pathfinder.admin")) {
                suggestions.addAll(Arrays.asList("reload", "lang"));
            }
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "create":
                    // Sin sugerencias para nombres de destino
                    break;

                case "ways":
                case "waypoints":
                    return pathwaysCommand.onTabComplete(sender, command, alias,
                            Arrays.copyOfRange(args, 1, args.length));

                case "go":
                case "navigate":
                    return pathGoCommand.onTabComplete(sender, command, alias,
                            Arrays.copyOfRange(args, 1, args.length));

                case "delete":
                case "remove":
                    if (args.length == 2) {
                        suggestions.addAll(plugin.getPathManager().getAllDestinationNames());
                    }
                    break;

                case "lang":
                case "language":
                    if (args.length == 2 && sender.hasPermission("pathfinder.admin")) {
                        suggestions.addAll(plugin.getLanguageManager().getAvailableLanguages());
                    }
                    break;
            }
        }

        return suggestions;
    }
}