# 🥚 Eggify

Eggify is a Fabric mod for Minecraft that gives thrown eggs a small chance to convert a mob into its spawn egg.

[![GitHub Release](https://img.shields.io/github/v/release/SwordfishBE/Eggify?display_name=release&logo=github)](https://github.com/SwordfishBE/Eggify/releases)
[![GitHub Downloads](https://img.shields.io/github/downloads/SwordfishBE/Eggify/total?logo=github)](https://github.com/SwordfishBE/Eggify/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/kOkcRRPb?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/eggify)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1503282?logo=curseforge&logoColor=white&label=CurseForge%20downloads)](https://www.curseforge.com/minecraft/mc-mods/eggify)

When an eggify succeeds:

- the mob disappears with portal-style particles and a sound is played
- the mob drops its spawn egg
- the dropped spawn egg keeps useful variant data when possible

Examples:

- `Gray Sheep` -> `Sheep Spawn Egg` with `Color: Gray`
- `Savanna Armorer Villager` -> `Villager Spawn Egg` with biome, profession and level
- `Screaming Goat` -> `Goat Spawn Egg` with `Type: Screaming`

---

## ✨ Features

- Configurable drop chance
- LuckPerms support with automatic detection
- Blacklist support for mobs that should never drop spawn eggs
- Variant preservation for many mobs
- Spawn egg tooltip details such as age, color, biome, profession, coat, personality or type

---
 
## 🎮 Commands

- `/eggify info`
  Shows the current Eggify configuration.
- `/eggify held`
  Shows debug info for the item in your main hand.
- `/eggify reload`
  Reloads the config file.

---

## 🔄 Permissions

Eggify supports the following permission nodes when `useLuckPerms=true`:

- `eggify.use`
  Allows the player to eggify mobs.
- `eggify.command`
  Allows the player to use `/eggify info` when `allowCommandPermissionNode=true`.
- `eggify.debug`
  Allows the player to use `/eggify held`.

Notes:

- Without LuckPerms, eggifying is available to everyone.
- Without active LuckPerms integration, `/eggify info` is controlled by `allowCommandPermissionNode`.
- Without active LuckPerms integration, `/eggify held` is controlled by `allowDebugCommand`.
- `/eggify reload` is always OP-only.
- When `useLuckPerms=true` and LuckPerms is installed, `allowCommandPermissionNode` and `allowDebugCommand` are ignored.
- OP players can still use the admin commands even when LuckPerms support is enabled.

---

## ⚙️ Configuration

The config file is created at:

`config/eggify.json`

The file includes inline comments to explain every option.

Default config:

```jsonc
// Eggify configuration
{
  // Chance in percent that a thrown egg converts a mob into its spawn egg.
  "dropChancePercent": 2.5,

  // When true, Eggify requires LuckPerms permission nodes.
  "useLuckPerms": false,

  // When true, non-OP players can use /eggify info with eggify.command.
  "allowCommandPermissionNode": false,

  // When true, non-OP players can use /eggify held without LuckPerms.
  "allowDebugCommand": false,

  // Enables the Modrinth update check.
  "enableUpdateCheck": true,

  // Mobs in this list can never be eggified.
  "blacklistedMobs": [
    "minecraft:ender_dragon",
    "minecraft:wither"
  ]
}
```

---

## 🌍 Variant Notes

Eggify tries to preserve useful identity data while stripping live runtime data like position, velocity, health, AI state, effects, inventory and trade state.

This means the mod aims to keep things like:

- baby vs adult
- sheep color
- rabbit coat
- mooshroom type
- frog type
- villager biome, profession and level
- wolf coat
- llama color and trader type
- cat coat
- goat type
- panda personality
- parrot color
- axolotl color

`Tropical Fish` is intentionally excluded from variant preservation.

---

## For Server Owners

Suggested LuckPerms examples:

```text
/lp user <player> permission set eggify.use true
/lp user <player> permission set eggify.command true
/lp user <player> permission set eggify.debug true
```

If you want non-OP players to use `/eggify info` without LuckPerms, also set:

```jsonc
"allowCommandPermissionNode": true
```

If you want non-OP players to use `/eggify held` without LuckPerms, also set:

```jsonc
"allowDebugCommand": true
```

Then run:

```text
/eggify reload
```

LuckPerms docs:

- Official wiki: [https://luckperms.net/wiki](https://luckperms.net/wiki)
- Command usage: [https://luckperms.net/wiki/Command-Usage](https://luckperms.net/wiki/Command-Usage)
- GitHub wiki mirror: [https://github.com/LuckPerms/LuckPerms/wiki/Command-Usage](https://github.com/LuckPerms/LuckPerms/wiki/Command-Usage)

---

## 📦 Installation

| Platform   | Link |
|------------|------|
| GitHub     | [Releases](https://github.com/SwordfishBE/Eggify/releases) |
| Modrinth | [Eggify](https://modrinth.com/mod/eggify) |
| CurseForge | [Eggify](https://www.curseforge.com/minecraft/mc-mods/eggify) |


1. Download the latest JAR from your preferred platform above.
2. Place the JAR in your server's `mods/` folder.
3. Make sure [Fabric API](https://modrinth.com/mod/fabric-api) is also installed.
4. Start Minecraft — the config file will be created automatically.

---

## 🧱 Building from Source

```bash
git clone https://github.com/SwordfishBE/Eggify.git
cd Eggify
chmod +x gradlew
./gradlew build
# Output: build/libs/eggify-<version>.jar
```

---

## 📄 License

Released under the [AGPL-3.0 License](LICENSE).
