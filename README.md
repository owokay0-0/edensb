# Eden

Eden is a client-only utility mod for Hypixel SkyBlock. It brings dungeon tools, customizable HUD elements, visibility options, rendering improvements, and tooltip enhancements together in one settings menu.

Configuration is organized into **Dungeons**, **Visuals**, and **Misc**. Options belonging to a feature are kept inside collapsible groups, and changes are written to disk automatically. There is no global “Only in SkyBlock” switch; features that require SkyBlock information simply stay inactive until that information is available.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5 or later
- Fabric API
- Java 25 or later

## Installing Eden

1. Set up Fabric Loader and Fabric API for Minecraft 26.3.
2. Copy the Eden jar into your Minecraft `mods` folder.
3. Start the game using the Fabric installation.

## Opening And Using The Menu

- Press `Right Shift` to open Eden while no other screen is active.
- Run `/eden` to open the configuration menu.
- Run `/eden hud` to reposition HUD components.
- Open **Misc → Chat Shortcuts** to bind any number of commands or messages to keyboard keys and mouse buttons.
- Expand a feature group to see its master toggle and related options.
- Eden stores toggles, colors, keybinds, HUD placement, and other preferences in `config/eden.json`.

## Dungeon Features

### Dungeon Score Meter

Shows the current dungeon score as a movable, scalable number. It is white by default and its color can be changed.

### Etherwarp

Draws a preview of the block an Etherwarp would reach before the teleport happens.

- Separate colors for successful and failed predictions
- Filled, outlined, and filled-with-outline styles
- Optional display of unsuccessful targets
- Server-position-based calculations
- Full-block and through-wall rendering controls
- Several left-click activation modes, including automatic sneaking
- Assignable toggle key

### DungeonBreaker

Improves client-side Dungeon Breaker mining when latency or mining fatigue interferes. It can protect secret-related blocks from accidental mining and locally remove valid mined blocks without waiting for the delayed server animation. A configurable keybind is included.

### Teammate Highlight

Marks dungeon teammates according to their detected class. Archer, Berserker, Tank, Mage, and Healer each have an individual color, and players are rendered with both filled and outlined highlighting. The feature can also be toggled with a keybind.

### Shitter Alert

Shows a pulsing `NOT 4/4` HUD alert when a dungeon teammate dies, disconnects, or vanishes from a previously full teammate roster. Its scale and position can be changed in the HUD editor.

## Visual Features

### Player Hider

Keeps nearby players from covering important parts of the screen. Players can be hidden within a chosen distance, all remote players can be hidden at once, or they can be faded using ghost mode and a custom opacity. Hidden players can optionally be ignored by targeting.

### Player Size

Rescales player models along the X, Y, and Z axes. Scaling may affect only the local player or every player, and name-tag positioning is corrected to match the changed model size.

### Hit Color

Substitutes Minecraft’s standard red damage flash with a user-selected color and transparency.

### Fullbright

Removes client-side darkness so the world remains clearly illuminated.

### Performance HUD

Shows FPS, estimated server TPS, and average latency as plain white text in a movable overlay.

- Choose which measurements are visible
- Horizontal and vertical layouts
- Simple white and tinted-white FPS and ping history graphs
- Adjustable placement and scale
- Precise placement in the HUD editor

### Render Optimizer

Allows selected visual effects and entities to be skipped when they create clutter or needless rendering work. Available filters cover falling blocks, lightning, experience orbs, death effects, dying armor stands, explosion particles, Archer passives, Healer fairies, Soul Weavers, tentacle heads, and the fire overlay.

### Name Replace

Detects the current Minecraft account name and substitutes it in rendered text. Both the replacement wording and its color are configurable; no original-name field needs to be entered manually.

### Zoom

Adds a hold-to-zoom control with ten strength levels. Level 1 uses an FOV of 50, while level 10 reaches an FOV of 1. The default key is `C`, scrolling can alter the intensity while zoomed, and releasing the key restores the previous FOV.

### Pet Overlay

Presents active pet progress in a configurable HUD element.

- Bar and circular designs, each with alternate variants
- Optional pet item and reversible icon placement
- Adjustable size and HUD-editor position
- Rarity-based or custom color themes
- Editable level, XP, and background colors
- Idle, hover, level-up, and changing-value animations
- Optional rainbow effects for the level, XP, and background

### Pressure Display

Shows Great Sea pressure in a movable widget. The activation threshold, scale, and theme can be changed, with final placement available through the HUD editor.

### Low HP Indicator

Adds a full-screen warning at low health. Its opacity is adjustable, and an optional heartbeat pulse can animate the warning.

### Drill Fuel Meter

Displays approximate remaining drill blocks as plain text in a movable and scalable overlay. The prediction can be disabled to show fuel percentage instead.

### Action Bar Cleanup

Removes information that is already displayed elsewhere. Pressure and drill-fuel text can each be hidden independently.

## Miscellaneous Features

### Diana QoL

Lets interactions pass through obstructive foliage during Diana-related gameplay. Grass and ferns, flowers, and bushes can each be controlled separately.

### Golden Fish CI

Permits fishing-rod use when a nearby Golden Fish would otherwise consume or block the interaction.

### Infinite Chat

Raises stored chat history to 10,000 messages and stops automatic history clearing while enabled.

### Missing Enchants

Checks an item against NotEnoughUpdates enchantment data and reports compatible enchantments that are absent. Hold `Shift` to reveal the complete list.

### Compact Pet Level

Condenses pet-level tooltip text without removing rarity colors or level-range information.

## Commands

- `/eden` — open the Eden configuration screen.
- `/eden hud` — launch the HUD positioning editor.
- `/eden auto` — report whether automatic updates are enabled.
- `/eden auto on` — enable automatic update checks and downloads.
- `/eden auto off` — disable automatic updates.
