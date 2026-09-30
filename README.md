# ChaosUtils

A large, fully **client-side** QoL and visual utility suite for **Minecraft 1.21.11**
(Fabric Loader 0.19.5+, Fabric API `0.141.6+1.21.11`, Java 21).

ChaosUtils is built around one idea: everything it does is presentation. Overlays read the
state the client already received, actions are triggered by the player exactly once, and not a
single packet is sent by the mod. That makes it safe on servers with anti-cheat - there is
nothing to detect, because there is no automation to detect.

---

## Feature overview (23 modules, ~120 settings)

**Radial menu (the centrepiece)**
* Hold `G`: a ring of slices appears around the cursor.
* Hover a slice, release the key - it runs exactly once.
* Arbitrary number of entries with display name, command/action, colour, item icon and type
  (send command, send chat, copy to clipboard, open ChaosUtils, toggle overlays).
* Full editor in the click GUI: add, edit, duplicate, reorder, disable and delete slices.

**HUD**
1. Entity health bars with exact numbers, damage flash and damage numbers (from vanilla synced entity data).
2. Ping / estimated TPS / tick time / FPS indicator.
3. Compact F3 replacement: coordinates, facing, biome, chunk, light level, time (game + real), dimension, server.
4. Armour & hand status with durability and enchantment short names ("Prot IV").
5. Durability warning below a configurable threshold, with icons and a soft alert.
6. Waypoints & death marker: beams, distances, off-screen arrows, auto-delete on arrival.
24. HUD Style: one place for overlay background, border, radius, shadow, scale, padding.

**Visual**
7. Crosshair designer: six shapes, thickness, gap, dot, colours, movement spread, aim fade, cooldown ring.
8. Night vision / brightness override on a hotkey (drives the vanilla brightness value, restores it exactly).
9. Smooth zoom on a held key, wheel-adjustable, with sensitivity compensation.
10. Perspective lock: the camera stays where you put it while your body moves - pure camera rotation, no hitbox or movement change.

**Inventory**
11. Container search: a search field on every container, matches highlighted, everything else dimmed.
12. Shulker box & bundle preview in the item tooltip, grouped and with fill level.
13. Item total counter in tooltips and above the hotbar, optionally including container contents.

**Chat & social**
14. Mention highlighter: toast + soft sound for your name and your keywords.
15. Chat history: survives reconnects, searchable, copyable, optional plain-text log, plus a
    filter (block list, regex, join/leave, duplicate collapsing).

**Audio**
16. Sound radar: circular direction display, colour per sound class, distance labels, list mode.
17. Enhanced subtitles: grouped, coloured, with direction arrows and distance; hides the vanilla list while active.
18. Unfocused volume reducer: fades down when you alt-tab and back when you return.

**Quality of life / performance**
19. Screenshot manager: popup on new screenshots, instant copy / crop presets / open folder.
20. Particle & explosion reducer: hide particle types you never look at, optional per-tick density cap.

---

## Hotkeys (all rebindable in Options → Controls)

| Key | Action |
| --- | --- |
| `G` (hold) | Radial menu |
| `Right Shift` | Open the ChaosUtils interface |
| `C` (hold) | Smooth zoom (mouse wheel adjusts) |
| `Left Alt` (hold) | Perspective lock |
| `Y` | Focus / clear the container search |
| unbound | Copy coordinates, chat history, screenshot manager, night vision, hide all overlays, add waypoint |

---

## Building

```bash
./gradlew build        # Linux / macOS
gradlew.bat build      # Windows
```

The complete Gradle wrapper is part of the repository (Gradle 9.2.1 — the version Loom 1.14
requires), so nothing has to be installed by hand. If the wrapper cannot download the
distribution (proxy, firewall, antivirus or a flaky connection — `Cannot use connection to
Gradle distribution … as it has been stopped`), see `docs/BUILD_TROUBLESHOOTING.md`: it lists the
pre-seed/offline ways to install the distribution and how to build with a system Gradle instead.

The compiled jar lands in `build/libs/chaosutils-1.0.0.jar` - drop it into `.minecraft/mods`
together with Fabric API.

Requirements:

| | |
| --- | --- |
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.141.6+1.21.11 or newer |
| Java | 21 |

1.21.11 is the last obfuscated Minecraft release and needs **Loom 1.14**; the build file is set
up for it.

---

## Project layout

```
src/main/java/dev/chaosutils/
├── ChaosUtils.java            client entrypoint: config, keybinds, tick loop, panic toggle
├── config/                    self-contained, versioned JSON config + Setting/Module model
├── core/                      keybinds, chat & sound buffers, TPS, clipboard, tick clock, API self-check
├── feature/                   one class per feature, grouped by category
│   ├── Feature.java           feature contract (tick, HUD, world join/leave, session disable)
│   ├── Features.java          registry, HUD element registration, cache pruning
│   ├── FeatureRegistry.java   the full module list in one readable place
│   ├── radial/                the radial menu feature + screen
│   ├── hud/ visual/ inventory/ chat/ audio/ qol/ performance/
├── gui/                       click GUI, component toolkit, theme, HUD editor, sub-screens
├── mixin/                     10 small, read-only integrations + 1 accessor (see docs/API_NOTES.md)
├── tools/                     mixin_check.py - verifies every mixin handler against the Minecraft sources
└── util/                      animation, drawing, projection, item/enchant/sound lookup, positions
```

`docs/API_NOTES.md` documents every 1.21.11 mapping decision, the mixin targets, the exact
signatures (verified against the 1.21.11 sources) and the fair-play rules the code follows.

`docs/ChaosUtils-source.md` is the same code collected into one document (one section per file,
in a readable order) for quick reading and reviewing; regenerate it after changing code with
`python3 tools/bundle_source.py`.

---

## Notes on performance

* Overlays draw with `GuiGraphics#fill`/text only - no render types, no shaders, no pipeline work.
* The heavy lists (inventory scans, container contents, durability, item counts) run on slow
  intervals (5-40 ticks) and reuse already allocated collections.
* Per-entity state (health bars) is dropped the moment an entity is removed via the `Entity`
  hook, so a long session cannot grow it.
* Every HUD element is wrapped in a guard: a failing overlay disables itself for the session
  instead of spamming the log or the frame budget.

MIT licensed.
