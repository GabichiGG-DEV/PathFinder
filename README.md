# 🧭 PathFinder GPS

[![Minecraft](https://img.shields.io/badge/Minecraft-1.16+-brightgreen.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Spigot](https://img.shields.io/badge/Spigot-Compatible-orange.svg)](https://www.spigotmc.org/)

**PathFinder GPS** es un plugin de navegación para servidores Minecraft que permite crear destinos y rutas visuales personalizadas usando partículas para guiar a los jugadores por tu mundo.

![PathFinder Banner](assets/banner.png)

---

## ✨ Características

- 🗺️ **Creación de Destinos** - Marca ubicaciones importantes en tu mundo
- 🛤️ **Rutas Personalizadas** - Crea caminos visuales con waypoints
- 🎨 **Partículas Personalizables** - Configura colores, tipos y densidad
- 🌍 **Soporte Multi-Mundo** - Funciona en múltiples mundos simultáneamente
- 🌐 **Multi-Idioma** - 5 idiomas incluidos (ES, EN, PT, FR, IT)
- 🔧 **Altamente Configurable** - Personaliza cada aspecto del plugin
- 🎮 **Fácil de Usar** - Sistema intuitivo de comandos
- 🔗 **Integración** - Compatible con DeluxeMenus, Citizens, ChestCommands y más

---

## 📦 Instalación

1. Descarga el archivo `PathFinder.jar`
2. Colócalo en la carpeta `plugins/` de tu servidor
3. Reinicia el servidor
4. ¡Listo! El plugin está activo

**Requisitos:**
- Servidor Spigot o Paper
- Java 8 o superior

---

## 🚀 Inicio Rápido

### Crear un Destino

```bash
/path set                    # Obtén la herramienta (Pala de Diamante)
[Click derecho en un bloque] # Marca la ubicación
/path create Spawn           # Crea el destino
```

### Navegar a un Destino

```bash
/path go Spawn              # Navega al destino
/path list                  # Lista todos los destinos
```

### Crear una Ruta Personalizada

```bash
/path ways create Spawn              # Inicia sesión de waypoints
[Click derecho para marcar puntos]   # Marca waypoints (de atrás hacia adelante)
/path ways Spawn save ruta_norte     # Guarda la ruta
```

---

## 📋 Comandos Principales

| Comando | Descripción |
|---------|-------------|
| `/path set` | Obtiene la herramienta para marcar destinos |
| `/path create <nombre>` | Crea un destino en la ubicación marcada |
| `/path go <destino>` | Navega a un destino |
| `/path list` | Muestra todos los destinos disponibles |
| `/path delete <destino>` | Elimina un destino y sus rutas |
| `/path ways create <destino>` | Inicia creación de ruta con waypoints |
| `/path ways <destino> save <ruta>` | Guarda la ruta creada |
| `/path ways undo` | Deshace el último waypoint |
| `/path reload` | Recarga la configuración |
| `/path lang <idioma>` | Cambia el idioma (es, en, pt, fr, it) |

**Alias:** `/path`, `/pathfinder`, `/pf`

---

## 🎯 Modos de Navegación

### Follow (Seguir Ruta)
```bash
/path go Spawn follow
```
Sigue la ruta con waypoints más cercana al jugador.

### NoFollow (Línea Recta)
```bash
/path go Spawn nofollow
```
Genera una línea recta directa ignorando rutas.

### Mix (Híbrido)
```bash
/path go Spawn mix
```
Línea recta al primer waypoint, luego sigue la ruta.

### Forzar Ruta Específica
```bash
/path go Spawn follow ruta_norte
/path go Spawn mix ruta_sur
```

### Para Otros Jugadores
```bash
/path go Spawn follow Steve
/path go Spawn mix Alex ruta_norte
```

---

## ⚙️ Configuración

El archivo `config.yml` se encuentra en `plugins/PathFinder/config.yml`

### Ejemplo de Configuración

```yaml
# Waypoint Settings
waypoints:
  max_waypoints: 30  # Máximo de waypoints por ruta

# Particle Settings
particles:
  waypoint_particle: "DUST"
  navigation_line_particle: "DUST"
  particle_density: 3  # 1-10

# Colors (RGB 0-255)
colors:
  distance_colors:
    close: [255, 85, 85]      # Rojo
    medium: [255, 255, 85]    # Amarillo
    far: [85, 255, 85]        # Verde
    very_far: [85, 85, 255]   # Azul

# Distances (blocks)
distances:
  close: 5.0
  medium: 15.0
  far: 30.0

# Language
default_language: "es"  # es, en, pt, fr, it
```

Después de editar, usa `/path reload` para aplicar cambios.

---

## 🔐 Permisos

| Permiso | Descripción | Default |
|---------|-------------|---------|
| `pathfinder.*` | Acceso completo | OP |
| `pathfinder.use` | Navegar a destinos | Todos |
| `pathfinder.list` | Ver lista de destinos | Todos |
| `pathfinder.set` | Obtener herramienta GPS | OP |
| `pathfinder.create` | Crear destinos y rutas | OP |
| `pathfinder.delete` | Eliminar destinos | OP |
| `pathfinder.admin` | Gestión y control de otros jugadores | OP |

### Ejemplo con LuckPerms

```bash
# Dar permisos a jugadores
/lp user Steve permission set pathfinder.use true

# Dar permisos a grupos
/lp group default permission set pathfinder.use true
/lp group admin permission set pathfinder.* true
```

---

## 🔗 Integración con Otros Plugins

PathFinder soporta el placeholder `%player%` para integrarse con otros plugins:

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

**Compatible con:** DeluxeMenus, Citizens, ChestCommands, CommandPanels, MyCommand, BossShopPro, y más.

---

## 🌐 Idiomas

PathFinder incluye 5 idiomas:

- 🇺🇸 English (`en`)
- 🇪🇸 Español (`es`)
- 🇧🇷 Português (`pt`)
- 🇫🇷 Français (`fr`)
- 🇮🇹 Italiano (`it`)

### Cambiar Idioma

```bash
/path lang es              # Cambiar a español
/path language en          # Cambiar a inglés
```

O edita `config.yml`:
```yaml
default_language: "es"
```

### Crear Idioma Personalizado

1. Copia un archivo de `plugins/PathFinder/languages/`
2. Renómbralo (ej: `custom.yml`)
3. Edita los mensajes
4. Usa `/path reload` y `/path lang custom`

---

## 📖 Documentación Completa

Para guías detalladas, visita nuestra [**Wiki**](../../wiki):

- [🚀 Instalación](../../wiki/Installation)
- [📝 Comandos Básicos](../../wiki/Basic-Commands)
- [🎯 Creación de Destinos](../../wiki/Creating-Destinations)
- [🛤️ Creación de Rutas](../../wiki/Creating-Routes)
- [🧭 Navegación](../../wiki/Navigation)
- [⚙️ Configuración](../../wiki/Configuration)
- [🌐 Idiomas](../../wiki/Languages)
- [🔐 Permisos](../../wiki/Permissions)
- [🔗 Integración](../../wiki/Integration)
- [❓ FAQ](../../wiki/FAQ)

---

## 💡 Ejemplos de Uso

### Servidor de Supervivencia
```bash
# Crear destinos principales
/path set
[Click] /path create Spawn
[Click] /path create TiendaComunal
[Click] /path create GranjaPublica

# Crear ruta segura
/path ways create Spawn
[Marca waypoints]
/path ways Spawn save ruta_principal
```

### Sistema de Tutorial con NPCs
```bash
# NPC Guía
/npc create Guia
/npc command add path go Spawn follow %player%
```

### Menú de Navegación
```yaml
# DeluxeMenus
spawn:
  material: COMPASS
  left_click_commands:
    - '[console] path go Spawn follow %player%'
    - '[message] &a¡Navegación activada!'
```

---

## 🎨 Personalización Visual

### Cambiar Partículas
```yaml
particles:
  navigation_line_particle: "FLAME"  # Efecto de fuego
  # DUST, FLAME, HEART, VILLAGER_HAPPY, PORTAL, END_ROD, etc.
```

### Colores Personalizados (Solo DUST)
```yaml
colors:
  distance_colors:
    close: [255, 0, 0]        # Rojo brillante
    medium: [255, 165, 0]     # Naranja
    far: [0, 255, 0]          # Verde
    very_far: [0, 100, 255]   # Azul
```

---

## ❓ Preguntas Frecuentes

### ¿Las rutas son necesarias?
No, los destinos funcionan sin rutas. PathFinder generará una línea recta automáticamente.

### ¿En qué dirección marco los waypoints?
**De atrás hacia adelante**: Desde el inicio de la ruta hacia el destino.
```
[Inicio] → WP1 → WP2 → WP3 → [Destino]
```

### ¿Cuántos waypoints puedo usar?
Por defecto 30, configurable en `config.yml` (recomendado máximo: 100).

### ¿Funciona en múltiples mundos?
Sí, pero solo puedes navegar a destinos del mismo mundo donde estás.

### ¿Cómo detengo la navegación?
La navegación se detiene automáticamente al llegar al destino, cambiar de mundo o desconectarse.

---

## 🤝 Contribuir

¿Quieres contribuir? ¡Genial!

- 🐛 **Reporta bugs** en [Issues](../../issues)
- 💡 **Sugiere features** en [Issues](../../issues)
- 🌐 **Comparte traducciones** personalizadas
- ⭐ **Dale una estrella** al proyecto

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

---

## 📞 Soporte

- 📖 **Wiki**: [Documentación Completa](../../wiki)
- 🐛 **Issues**: [Reportar Problemas](../../issues)
- 💬 **Discord**: [Servidor de Soporte](#) *(añade tu enlace)*

---

## 🙏 Agradecimientos

Gracias a todos los que han contribuido y apoyado el desarrollo de PathFinder GPS.

---

<div align="center">

**[⬆ Volver arriba](#-pathfinder-gps)**

Hecho con ❤️ para la comunidad de Minecraft

</div>
