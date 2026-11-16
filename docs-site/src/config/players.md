---
title: Player Designs
description: Configure custom MiniMessage and Vault designs in players.yml.
---

# Player Designs (players.yml)

This is where the visual magic happens. The `players.yml` file allows you to define **unlimited groups** using the modern *MiniMessage* engine, interpolated with the versatility of *PlaceholderAPI*.

---

## 🎨 Understanding MiniMessage

Instead of using old and limited color codes like `&c` or `&l`, you can now use highly descriptive HTML tags.

| Tag | Result |
|-----|--------|
| `<red>` | Pure red text |
| `<#ff00ff>` | Text using custom HEX code |
| `<bold>` | **Bold** text |
| `<gradient:red:blue>` | Generates a smooth transition between two or more colors |

---

## 👥 Default Format

Any user who does not fit into a rank hierarchy will fall into this design:

```yaml
players:
  default_format:
    - "<yellow>Player</yellow>"
    - "<white><PLAYER></white>"
```
*You will notice that you can create as many lines as you want simply by adding hyphens (`-`) to the list.*

---

## 👑 Advanced Ranks & Vault

You can create nametag groups that exactly match your rank name in **LuckPerms** (or any other Vault-compatible permission system). The plugin reads the player's *Primary Group* and looks for its equivalent here.

```yaml
  groups:
    vip:
      - "<gradient:gold:yellow>⭐ VIP User</gradient>"
      - "<white><PLAYER></white>"
    admin:
      - "<red>:rank_dev: Supreme Admin</red>"
      - "<gradient:red:dark_red><bold><PLAYER></bold></gradient>"
```

### 🖼️ Integration with ItemsAdder / Oraxen

If you look closely at the `admin` format, you'll notice the text `:rank_dev:`. 

If you have **ItemsAdder** or any Resource Pack installed on your server, the player's client will read this and automatically replace the colons with an **actual font image** rendered above the admin's head.

::: tip 💡 DESIGN TIP
Avoid making Nametags more than 3 lines long for regular players. Excessive lines can obstruct vision in PvP or dense builds.
:::

---

## 🏰 Spawn-Area Nametags (`inspawn`) — new in 1.7.0

Add an `inspawn:` section to `players.yml` and every player standing within `radius` blocks of the **world spawn point** automatically gets this nametag instead of their normal one. The moment they walk away, their regular tag comes back — the check runs every render tick, no commands needed.

```yaml
inspawn:
  radius: 16
  default_format:
    - "<gradient:red:gold>WELCOME TO SPAWN</gradient>"
    - "<white><PLAYER></white>"
  groups:
    admin:
      - "<gold>[SPAWN]</gold> <red>Admin</red> <white><PLAYER></white>"
```

* `radius` — distance in blocks around the world spawn point (default `16`).
* `default_format` — used by players whose group has no spawn override.
* `groups` — optional per-rank overrides, same rules as the normal `groups` section.
* Remove the whole section (or set `radius: 0`) to disable the feature.

::: tip 💡
Each world uses **its own** spawn point, so the tag appears near the spawn of any world the player is in. Reload with `/enametag reload` after editing the file.
:::

---

## 🧑 Sprite & Head Tags (`<head>`, `<sprite>`) — new in 1.7.0

On **Paper / Folia 1.21.9+** (Minecraft's *object text components*, adventure 4.25+) nametags can now embed real player heads and atlas sprites:

| Tag | Result |
|-----|--------|
| `<head>` | Head of the player wearing the tag (on pets: head of the owner) |
| `<head:PlayerName>` | Head of any player by name |
| `<head:069a79f4-44e9-4726-a5be-fca90e38aaf5>` | Head by UUID |
| `<head:PlayerName:false>` | Head with the hat layer disabled |
| `<sprite:blocks:block/stone>` | Atlas sprite, atlas without namespace |
| `<sprite:"minecraft:items":item/diamond_helmet>` | Sprite from a named atlas — quote it, because the atlas id contains `:` |

Example:

```yaml
default_format:
  - "<sprite:blocks:block/stone> <gray>Miner</gray>"
  - "<white><head> <PLAYER></white>"
```

::: warning ⚠️ REQUIREMENTS
* **Server:** Paper/Folia 1.21.9 or newer. On Spigot or older Paper builds the tags are stripped automatically (with a one-time console warning) instead of showing raw text — nothing breaks.
* **Client:** players need a 1.21.9+ client; older clients connected via ViaVersion show an empty space where the icon would be.
* Sprites must exist in the client's texture atlas — vanilla examples: `minecraft:blocks`, `minecraft:items`, `minecraft:gui`. Custom atlases from resource packs work too.
:::
