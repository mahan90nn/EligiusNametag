# Changelog

## [1.7.2](https://github.com/mahan90nn/EligiusNametag/releases/tag/v1.7.2) (2026-11-19)


### Bug Fixes

* **nametag no longer disappears when sneaking (the shift bug).** Root cause: the plugin deliberately hid a player's nametag from all viewers while they sneaked (vanilla-like), which hid the tag on every shift press and brought it back on release. Hiding on sneak is now opt-in via `config.yml` `sneak-hides-nametag: false` (default keeps the tag visible).
* **nametag no longer sinks into the player while sneaking.** With the tag visible during sneak, a sneaking player's passenger mount point drops 0.3 blocks (pose height 1.8 → 1.5, measured on Paper 1.21.11 via entity NBT), which pushed the mounted TextDisplay into the player model. The renderer now lifts the display transformation by 0.3 while the target sneaks — verified live: the rendered height (entity position + translation) is identical standing and sneaking.
* **renderer:** a nametag line the client encoder rejects (e.g. a malformed head value) no longer kills the whole render task — which left *every* nametag missing until relog. The failure is contained to that line and logged with its root cause instead.


## [1.7.1](https://github.com/mahan90nn/EligiusNametag/releases/tag/v1.7.1) (2026-11-17)


### Features

* **nametag:** `<head:BASE64>` and texture-hash heads — a head tag argument that is a base64 texture value (the "Value" field from head sites / `/give`) or a 64-char textures.minecraft.net hash now renders as a player head with that skin; the optional `:false` hat-layer suffix works too.
* **players.yml:** the spawn-area section now accepts `in-spawn:`, `in_spawn:` and `inspawn:` spellings, plus a simple statement form (`in-spawn:` followed directly by a list of lines) in addition to the structured `radius`/`default_format`/`groups` form.


## [1.7.0](https://github.com/mahan90nn/EligiusNametag/releases/tag/v1.7.0) (2026-11-16)


### Features

* **nametag:** add `<head>` and `<sprite>` object component tags (Paper 1.21.9+ / adventure 4.25+). A bare `<head>` resolves to the nametag's own player (or the pet's owner); unknown runtimes strip the tags gracefully instead of showing raw text.
* **players.yml:** new `inspawn:` section (`radius`, `default_format`, `groups`) — shows a dedicated nametag while the player stands within `radius` blocks of the world spawn point, switching back automatically when they leave.


### Dependencies

* adventure-api / minimessage / legacy-serializer bumped from 4.17.0 to 4.25.0.

## [1.6.1](https://github.com/Eligiusmc/EligiusNametag/compare/v1.6.0...v1.6.1) (2026-11-02)


### Bug Fixes

* **core:** eject nametag passengers before teleport to unblock vehicle TP ([c4dc4d4](https://github.com/Eligiusmc/EligiusNametag/commit/c4dc4d43b27ca0639d890c57e78467750135a1be))
* **core:** eject TextDisplay passengers to unblock teleport and mounting ([73eb962](https://github.com/Eligiusmc/EligiusNametag/commit/73eb962d5e3e74dc0ebe50313b5de370715c2b35))
* **nametag:** enforce config view distance server-side ([793d0e1](https://github.com/Eligiusmc/EligiusNametag/commit/793d0e17bc09b83778ae5e2dd12d00c850c87705)), closes [#45](https://github.com/Eligiusmc/EligiusNametag/issues/45)
* **nametag:** enforce config view distance server-side and stop evicting named pets ([db853ab](https://github.com/Eligiusmc/EligiusNametag/commit/db853ab8df10a941441a7fb5ae73a7525716d7b2))
* **security:** validate lang code and declare plugin permissions ([ace9920](https://github.com/Eligiusmc/EligiusNametag/commit/ace9920315ba7a5bb71f3adcf90c6f2bf5c2fd67))

## [1.6.0](https://github.com/Eligiusmc/EligiusNametag/compare/v1.5.2...v1.6.0) (2026-06-03)


### Features

* **nametag:** add pure spigot compatibility and multiplatform support ([3b348cd](https://github.com/Eligiusmc/EligiusNametag/commit/3b348cd3c4ab75ae9ef9086c53fb8e60d9a4d659))

## [1.5.2](https://github.com/Eligiusmc/EligiusNametag/compare/v1.5.1...v1.5.2) (2026-06-03)


### Bug Fixes

* auto-publish to modrinth and hangar ([2639e1c](https://github.com/Eligiusmc/EligiusNametag/commit/2639e1ce77d01a5124ad8da267c5ec5e392c18cc))

## [1.5.1](https://github.com/Eligiusmc/EligiusNametag/compare/v1.5.0...v1.5.1) (2026-06-02)


### Bug Fixes

* force release generation for build artifact changes ([d6b84bd](https://github.com/Eligiusmc/EligiusNametag/commit/d6b84bd804530735ac4e24b656efd43fa9ab836a))

## [1.5.0](https://github.com/Eligiusmc/EligiusNametag/compare/v1.4.0...v1.5.0) (2026-06-02)


### Features

* implement native redis pub/sub and fix ghost nametags ([#26](https://github.com/Eligiusmc/EligiusNametag/issues/26), [#28](https://github.com/Eligiusmc/EligiusNametag/issues/28)) ([04e6bef](https://github.com/Eligiusmc/EligiusNametag/commit/04e6befe8f04ba48525176acc4330feb199ea525))

## [1.4.0](https://github.com/Eligiusmc/EligiusNametag/compare/v1.3.1...v1.4.0) (2026-06-02)


### Features

* bStats integration and telemetry ([73f0dd1](https://github.com/Eligiusmc/EligiusNametag/commit/73f0dd156d3d5f7655ebce3027421ede680b0026))

## [1.3.1](https://github.com/Eligiusmc/EligiusNametag/compare/v1.3.0...v1.3.1) (2026-05-31)
## [1.3.1] - 2026-06-02

### Added
- **bStats Integration:** We now collect anonymous usage metrics (Server platform, Database type, etc.) to help guide future development. You can opt out via your global `bStats/config.yml`.

### Bug Fixes

* resolve TextDisplay memory leaks, fly-in interpolation bugs and update UTF-8 lang ([307b42a](https://github.com/Eligiusmc/EligiusNametag/commit/307b42a1fdf4549b5966ca0f5eaf09b1b98609e2))

## [1.3.0] - 2026-05-30


### Features

* **core:** migrate to native Bukkit TextDisplays and drop ProtocolLib ([526a1a9](https://github.com/Eligiusmc/EligiusNametag/commit/526a1a9aa482ef4e7b6f14654b3e570007100a8c))

## [1.2.0](https://github.com/Eligiusmc/EligiusNametag/compare/v1.1.0...v1.2.0) (2026-05-31)


### Features

* **wiki & ci:** i18n support, animated dark theme, and automated releases enhancement ([#11](https://github.com/Eligiusmc/EligiusNametag/issues/11)) ([2bae140](https://github.com/Eligiusmc/EligiusNametag/commit/2bae140cc64e5936d33a8778c59a677d2345af69))

## [1.1.0](https://github.com/Eligiusmc/EligiusNametag/compare/v1.0.0...v1.1.0) (2026-05-31)


### Features

* implement release please automation and update checker ([#8](https://github.com/Eligiusmc/EligiusNametag/issues/8)) ([#9](https://github.com/Eligiusmc/EligiusNametag/issues/9)) ([2c07851](https://github.com/Eligiusmc/EligiusNametag/commit/2c0785136b85f267c1d01cfb44402845b8b49af6))


### Bug Fixes

* enforce github pages deployment and set correct vitepress base ([f8d9e4b](https://github.com/Eligiusmc/EligiusNametag/commit/f8d9e4b746e2f62dae09a9479d233b0785f4c18d))
* resolve release-please unsupported extraFile type regex ([573c30c](https://github.com/Eligiusmc/EligiusNametag/commit/573c30cdf0cd97b48b80cef8e90bff077855e6bf))
