# 🗺️ PathfinderGPS

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-Required-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-red.svg)](https://github.com/GabiChigg/PathfinderGPS/releases)

**PathfinderGPS** is a professional GPS navigation system for Minecraft Paper 1.21.1+ that creates beautiful particle paths to guide players to their destinations.

![PathfinderGPS Demo](https://via.placeholder.com/800x400/1a1a1a/00ff00?text=PathfinderGPS+Demo)

## ✨ Features

- 🎯 **Multiple Routes per Destination** - Create several paths to the same location
- 🌈 **Customizable Particles** - Configure colors, types, and density
- 🗣️ **Multi-Language Support** - English, Spanish, Portuguese (+ custom)
- 🎨 **Visual Waypoint System** - Mark up to 30 waypoints with visual feedback
- 🔄 **Real-time Path Preview** - See your route while creating it
- ↩️ **Undo System** - Made a mistake? Just undo the last waypoint
- 🎮 **Admin Controls** - Force navigation for other players
- 📍 **Direct Line Fallback** - Works even without custom routes
- ⚙️ **Fully Configurable** - Colors, sounds, distances, and more
- 🔄 **Hot Reload** - Reload config without restarting server

## 📥 Installation

1. Download the latest release from [Releases](https://github.com/GabiChigg/PathfinderGPS/releases)
2. Place `PathfinderGPS-1.0.0.jar` in your server's `plugins/` folder
3. Restart your server
4. Configure `plugins/PathfinderGPS/config.yml` (optional)
5. Choose your language with `/pathfinder lang <en|es|pt>`

## 🚀 Quick Start

### Creating Your First Route

```bash
# 1. Get the destination tool
/pathset

# 2. Right-click on the destination block
[Right-click with diamond shovel]

# 3. Create the destination
/pathcreate spawn

# 4. Get waypoint tool to create a custom route
/pathways create spawn

# 5. Mark waypoints along your desired path
[Right-click blocks with recovery compass]

# 6. Save the route
/pathways spawn save main_route

# 7. Navigate to your destination
/pathgo spawn
```

### Using Navigation

```bash
# Navigate to a destination
/pathgo spawn

# Stop navigation
/pathstop

# View all destinations
/pathlist

# Preview a specific route
/pathways spawn view main_route
```

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pathset` | Get destination marking tool | `pathfinder.set` |
| `/pathcreate <name>` | Create a destination | `pathfinder.create` |
| `/pathways create <dest>` | Start waypoint session | `pathfinder.create` |
| `/pathways undo` | Remove last waypoint | `pathfinder.create` |
| `/pathways <dest> save <route>` | Save current route | `pathfinder.create` |
| `/pathways <dest> view <route>` | Preview a route | `pathfinder.create` |
| `/pathways stop` | Stop route preview | `pathfinder.create` |
| `/pathgo <dest> [player]` | Start navigation | `pathfinder.use` |
| `/pathstop` | Stop navigation | `pathfinder.use` |
| `/pathlist` | List all destinations | `pathfinder.list` |
| `/pathdelete <name>` | Delete a destination | `pathfinder.delete` |
| `/pathfinder reload` | Reload configuration | `pathfinder.admin` |
| `/pathfinder lang <language>` | Change language | `pathfinder.admin` |

## 🎨 Configuration

PathfinderGPS is highly customizable. Edit `config.yml` to change:

- Maximum waypoints per route (1-100)
- Particle types and density
- RGB colors for different distances
- Sound effects and volume
- Distance thresholds

### Example config.yml

```yaml
waypoints:
  max_waypoints: 30

particles:
  waypoint_particle: "DUST"
  session_line_particle: "DUST"
  navigation_line_particle: "DUST"
  particle_density: 3

colors:
  distance_colors:
    close: [255, 85, 85]    # Red
    medium: [255, 255, 85]  # Yellow
    far: [85, 255, 85]      # Green
    very_far: [85, 85, 255] # Blue

distances:
  close: 5.0
  medium: 15.0
  far: 30.0
```

## 🌍 Languages

Available languages:
- 🇺🇸 English (`en.yml`)
- 🇪🇸 Spanish (`es.yml`)
- 🇧🇷 Portuguese (`pt.yml`)

Create custom languages by copying any `.yml` file in `languages/` folder!

## 🔧 Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `pathfinder.*` | All permissions | OP |
| `pathfinder.set` | Create destinations | OP |
| `pathfinder.create` | Create routes | OP |
| `pathfinder.use` | Use navigation | All players |
| `pathfinder.list` | List destinations | All players |
| `pathfinder.delete` | Delete destinations | OP |
| `pathfinder.admin` | Admin commands | OP |

## 🎯 Use Cases

- **Spawn Protection** - Guide new players to spawn
- **Quest Systems** - Direct players to quest locations
- **Server Tours** - Create guided tours with multiple routes
- **PvP Arenas** - Help players find different arena entrances
- **Minigames** - Guide players through complex game maps
- **RPG Servers** - Create immersive navigation for cities

## 🔌 API Usage

Other plugins can integrate with PathfinderGPS:

```java
// Get PathfinderGPS instance
PathfinderGPS pathfinder = (PathfinderGPS) Bukkit.getPluginManager().getPlugin("PathfinderGPS");

// Start navigation for a player
Player player = ...;
pathfinder.getServer().dispatchCommand(Bukkit.getConsoleSender(), 
    "pathgo spawn " + player.getName());
```

## 📖 Wiki

For detailed guides, tutorials, and advanced usage, visit our [Wiki](https://github.com/GabiChigg/PathfinderGPS/wiki).

## 🐛 Bug Reports

Found a bug? Please report it on our [Issues](https://github.com/GabiChigg/PathfinderGPS/issues) page.

## 💡 Feature Requests

Have an idea? Open a [Feature Request](https://github.com/GabiChigg/PathfinderGPS/issues/new?labels=enhancement)!

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) first.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**GabiChigg**
- GitHub: [@GabiChigg](https://github.com/GabiChigg)

## 🌟 Support

If you like PathfinderGPS, please ⭐ star this repository!

## 📸 Screenshots

### Waypoint Creation
![Creating Waypoints](https://via.placeholder.com/600x400/1a1a1a/00ffff?text=Waypoint+Creation)

### Navigation View
![Navigation](https://via.placeholder.com/600x400/1a1a1a/ffff00?text=Navigation+View)

### Route Preview
![Route Preview](https://via.placeholder.com/600x400/1a1a1a/ff00ff?text=Route+Preview)

---

Made with ❤️ by GabiChigg
