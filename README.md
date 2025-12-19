# 🧭 PathFinder GPS

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8+-brightgreen.svg)](https://www.spigotmc.org/)
[![Spigot](https://img.shields.io/badge/Spigot-Compatible-orange.svg)](https://www.spigotmc.org/)
[![Paper](https://img.shields.io/badge/Paper-Compatible-00ADD8.svg)](https://papermc.io/)
[![Purpur](https://img.shields.io/badge/Purpur-Compatible-blueviolet.svg)](https://purpurmc.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**PathFinder GPS** is a navigation plugin for Minecraft servers that allows you to create destinations and custom visual routes using particles to guide players through your world.

![PathFinder Banner](assets/banner.png)

---

## ✨ Features

- 🗺️ **Destination Creation** - Mark important locations in your world
- 🛤️ **Custom Routes** - Create visual paths with waypoints
- 🎨 **Customizable Particles** - Configure colors, types, and density
- 🌍 **Multi-World Support** - Works across multiple worlds simultaneously
- 🌐 **Multi-Language** - 5 languages included (EN, ES, PT, FR, IT)
- 🔧 **Highly Configurable** - Customize every aspect of the plugin
- 🎮 **Easy to Use** - Intuitive command system
- 🔗 **Integration** - Compatible with DeluxeMenus, Citizens, ChestCommands, and more

---

## 📦 Installation

1. Download the `PathFinder.jar` file
2. Place it in your server's `plugins/` folder
3. Restart the server
4. Done! The plugin is active

**Requirements:**
- Spigot, Paper, or Purpur server
- Java 8 or higher

---

## 🚀 Quick Start

### Create a Destination

```bash
/path set                    # Get the tool (Diamond Shovel)
[Right-click on a block]     # Mark the location
/path create Spawn           # Create the destination
```
![Pathset](assets/1.gif)
![Pathset](assets/2.gif)

### Navigate to a Destination

```bash
/path go Spawn              # Navigate to the destination
/path list                  # List all destinations
```
![Pathset](assets/3.gif)

### Create a Custom Route

```bash
/path ways create Spawn              # Start waypoint session
[Right-click to mark points]         # Mark waypoints (from start to finish)
/path ways Spawn save north_route    # Save the route
```
![Pathset](assets/4.gif)
![Pathset](assets/5.gif)
---

## 📋 Main Commands

| Command | Description |
|---------|-------------|
| `/path set` | Get the tool to mark destinations |
| `/path create <name>` | Create a destination at the marked location |
| `/path go <destination>` | Navigate to a destination |
| `/path list` | Show all available destinations |
| `/path delete <destination>` | Delete a destination and its routes |
| `/path ways create <destination>` | Start route creation with waypoints |
| `/path ways <destination> save <route>` | Save the created route |
| `/path ways undo` | Undo the last waypoint |
| `/path reload` | Reload the configuration |
| `/path lang <language>` | Change language (en, es, pt, fr, it) |

**Aliases:** `/path`, `/pathfinder`, `/pf`

---

## 🎯 Navigation Modes

### Follow (Follow Route)
```bash
/path go Spawn follow
```
Follows the waypoint route closest to the player.

![Pathset](assets/6.gif)

### NoFollow (Straight Line)
```bash
/path go Spawn nofollow
```
Generates a direct straight line ignoring routes.

![Pathset](assets/7.gif)

### Mix (Hybrid)
```bash
/path go Spawn mix
```
Straight line to the first waypoint, then follows the route.

![Pathset](assets/8.gif)

### Force Specific Route
```bash
/path go Spawn follow north_route
/path go Spawn mix south_route
```

### For Other Players
```bash
/path go Spawn follow Steve
/path go Spawn mix Alex north_route
```

---

## ⚙️ Configuration

The `config.yml` file is located at `plugins/PathFinder/config.yml`

### Configuration Example

```yaml
# Waypoint Settings
waypoints:
  max_waypoints: 30  # Maximum waypoints per route

# Particle Settings
particles:
  waypoint_particle: "DUST"
  navigation_line_particle: "DUST"
  particle_density: 3  # 1-10

# Colors (RGB 0-255)
colors:
  distance_colors:
    close: [255, 85, 85]      # Red
    medium: [255, 255, 85]    # Yellow
    far: [85, 255, 85]        # Green
    very_far: [85, 85, 255]   # Blue

# Distances (blocks)
distances:
  close: 5.0
  medium: 15.0
  far: 30.0

# Language
default_language: "en"  # en, es, pt, fr, it
```

After editing, use `/path reload` to apply changes.

---

## 🔐 Permissions

| Permission | Description | Default |
|---------|-------------|---------|
| `pathfinder.*` | Full access | OP |
| `pathfinder.use` | Navigate to destinations | Everyone |
| `pathfinder.list` | View destination list | Everyone |
| `pathfinder.set` | Get GPS tool | OP |
| `pathfinder.create` | Create destinations and routes | OP |
| `pathfinder.delete` | Delete destinations | OP |
| `pathfinder.admin` | Management and control other players | OP |

### Example with LuckPerms

```bash
# Give permissions to players
/lp user Steve permission set pathfinder.use true

# Give permissions to groups
/lp group default permission set pathfinder.use true
/lp group admin permission set pathfinder.* true
```

---

## 🔗 Integration with Other Plugins

PathFinder supports the `%player%` placeholder for integration with other plugins:

### DeluxeMenus
```yaml
spawn_button:
  left_click_commands:
    - '[console] path go Spawn follow %player%'
```

### Citizens (NPCs)
```bash
/npc command add path go Spawn follow %player%
```

### ChestCommands
```yaml
spawn-button:
  COMMAND: 'console: path go Spawn follow %player%'
```

**Compatible with:** DeluxeMenus, Citizens, ChestCommands, CommandPanels, MyCommand, BossShopPro, and more.

---

## 🌐 Languages

PathFinder includes 5 languages:

- 🇺🇸 English (`en`)
- 🇪🇸 Español (`es`)
- 🇧🇷 Português (`pt`)
- 🇫🇷 Français (`fr`)
- 🇮🇹 Italiano (`it`)

### Change Language

```bash
/path lang en              # Change to English
/path language es          # Change to Spanish
```

Or edit `config.yml`:
```yaml
default_language: "en"
```

### Create Custom Language

1. Copy a file from `plugins/PathFinder/languages/`
2. Rename it (e.g., `custom.yml`)
3. Edit the messages
4. Use `/path reload` and `/path lang custom`

---

## 📖 Full Documentation

For detailed guides, visit our [**Wiki**](../../wiki):

- [🚀 Installation](../../wiki/Installation)
- [📝 Basic Commands](../../wiki/Basic-Commands)
- [🎯 Creating Destinations](../../wiki/Creating-Destinations)
- [🛤️ Creating Routes](../../wiki/Creating-Routes)
- [🧭 Navigation](../../wiki/Navigation)
- [⚙️ Configuration](../../wiki/Configuration)
- [🌐 Languages](../../wiki/Languages)
- [🔐 Permissions](../../wiki/Permissions)
- [🔗 Integration](../../wiki/Integration)
- [❓ FAQ](../../wiki/FAQ)

---

## 💡 Usage Examples

### Survival Server
```bash
# Create main destinations
/path set
[Click] /path create Spawn
[Click] /path create CommunityShop
[Click] /path create PublicFarm

# Create safe route
/path ways create Spawn
[Mark waypoints]
/path ways Spawn save main_route
```

### Tutorial System with NPCs
```bash
# Guide NPC
/npc create Guide
/npc command add path go Spawn follow %player%
```

### Navigation Menu
```yaml
# DeluxeMenus
spawn:
  material: COMPASS
  left_click_commands:
    - '[console] path go Spawn follow %player%'
    - '[message] &aNavigation activated!'
```

---

## 🎨 Visual Customization

### Change Particles
```yaml
particles:
  navigation_line_particle: "FLAME"  # Fire effect
  # DUST, FLAME, HEART, VILLAGER_HAPPY, PORTAL, END_ROD, etc.
```

### Custom Colors (DUST only)
```yaml
colors:
  distance_colors:
    close: [255, 0, 0]        # Bright red
    medium: [255, 165, 0]     # Orange
    far: [0, 255, 0]          # Green
    very_far: [0, 100, 255]   # Blue
```

---

## ❓ Frequently Asked Questions

### Are routes necessary?
No, destinations work without routes. PathFinder will automatically generate a straight line.

### In what direction do I mark waypoints?
**From start to finish**: From the beginning of the route towards the destination.
```
[Start] → WP1 → WP2 → WP3 → [Destination]
```

### How many waypoints can I use?
30 by default, configurable in `config.yml` (recommended maximum: 100).

### Does it work across multiple worlds?
Yes, but you can only navigate to destinations in the same world you're in.

### How do I stop navigation?
Navigation stops automatically when reaching the destination, changing worlds, or disconnecting.

---

## 🤝 Contributing

Want to contribute? Great!

- 🐛 **Report bugs** in [Issues](../../issues)
- 💡 **Suggest features** in [Issues](../../issues)
- 🌐 **Share custom translations**
- ⭐ **Star the project**

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## 📞 Support

- 📖 **Wiki**: [Full Documentation](../../wiki)
- 🐛 **Issues**: [Report Problems](../../issues)
- 💬 **Discord**: [Support Server](#) *(add your link)*

---

## 🙏 Acknowledgments

Thanks to everyone who has contributed and supported the development of PathFinder GPS.

---

<div align="center">

**[⬆ Back to top](#-pathfinder-gps)**

Made with ❤️ for the Minecraft community

</div>
