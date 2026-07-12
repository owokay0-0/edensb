package dev.eden.client

import dev.eden.client.feature.EdenFeatures
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.*
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/** MoulConfig-backed replacement for Eden's former hand-written menu. */
object EdenMoulConfig {
    private var config = EdenConfigRoot()

    fun reload() {
        config = EdenConfigRoot()
        config.saveRunnables.add(Runnable { config.writeBack() })
    }

    fun open() {
        reload()
        val processor = MoulConfigProcessor(config)
        BuiltinMoulConfigGuis.addProcessors(processor)
        ConfigProcessorDriver(processor).processConfig(config)
        IMinecraft.INSTANCE.openWrappedScreen(MoulConfigEditor(processor))
    }
}

class EdenConfigRoot : Config() {
    @Category(name = "Dungeons", desc = "Dungeon helpers and overlays")
    var dungeons = DungeonsCategory()

    @Category(name = "Visuals", desc = "Rendering, HUDs and visual customization")
    var visuals = VisualsCategory()

    @Category(name = "Misc", desc = "Quality-of-life features and reminders")
    var misc = MiscCategory()

    override fun getTitle(): StructuredText = StructuredText.of("Eden")
    override fun isValidRunnable(runnableId: Int) = false

    fun writeBack() {
        dungeons.save()
        visuals.save()
        misc.save()
        EdenFeatures.loadFromConfig()
    }
}

class DungeonsCategory {
    @ConfigEditorAccordion(id = 1)
    @ConfigOption(name = "Dungeon Score Meter", desc = "Dungeon score HUD settings")
    var scoreGroup = false

    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Dungeon Score Meter", desc = "Show the current dungeon score on the HUD") @ConfigEditorBoolean
    var scoreEnabled = EdenFeatures.dungeonScoreMeterEnabled
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Anchor", desc = "HUD anchor") @ConfigEditorDropdown(values = ["Top Left", "Middle Left", "Bottom Left", "Top Right", "Middle Right", "Bottom Right", "Top Middle", "Bottom Middle"])
    var scoreAnchor = EdenFeatures.dungeonScoreMeterAnchor
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f)
    var scoreScale = EdenFeatures.dungeonScoreMeterScale * 100f
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Edit Score HUD", desc = "Position the score meter") @ConfigEditorButton(buttonText = "Open")
    val editScore = Runnable { Minecraft.getInstance().setScreen(EdenHudEditorScreen(null, EdenHudWidget.DUNGEON_SCORE)) }
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Theme", desc = "Meter appearance") @ConfigEditorDropdown(values = ["Rank", "Gradient"])
    var scoreTheme = EdenFeatures.dungeonScoreMeterTheme
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Gradient Color 1", desc = "First gradient color") @ConfigEditorColour
    var scoreColor1 = legacy(EdenFeatures.dungeonScoreGradientColor1.argb)
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Gradient Color 2", desc = "Second gradient color") @ConfigEditorColour
    var scoreColor2 = legacy(EdenFeatures.dungeonScoreGradientColor2.argb)
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Gradient Rotation", desc = "Gradient angle in degrees") @ConfigEditorSlider(minValue = 0f, maxValue = 360f, minStep = 1f)
    var scoreRotation = EdenFeatures.dungeonScoreMeterGradientRotation * 360f

    @ConfigEditorAccordion(id = 2)
    @ConfigOption(name = "Etherwarp", desc = "Etherwarp settings")
    var etherwarpGroup = false

    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp", desc = "Etherwarp targeting helpers") @ConfigEditorBoolean
    var etherwarp = EdenFeatures.etherwarpEnabled
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Show Guess", desc = "Show the predicted target") @ConfigEditorBoolean var etherGuess = EdenFeatures.etherwarpShowGuess
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Color", desc = "Target color") @ConfigEditorColour var etherColor = legacy(EdenFeatures.etherwarpColor.argb)
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Show Failed", desc = "Show failed targets") @ConfigEditorBoolean var etherFailed = EdenFeatures.etherwarpShowFailed
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Fail Color", desc = "Failed target color") @ConfigEditorColour var etherFailColor = legacy(EdenFeatures.etherwarpFailColor.argb)
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Render Style", desc = "Target box style") @ConfigEditorDropdown(values = ["Filled", "Outline", "Filled Outline"]) var etherStyle = EdenFeatures.etherwarpRenderStyle
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Use Server Position", desc = "Use server position") @ConfigEditorBoolean var etherServer = EdenFeatures.etherwarpUseServerPosition
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Full Block", desc = "Draw a full block") @ConfigEditorBoolean var etherFull = EdenFeatures.etherwarpFullBlock
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Depth", desc = "Render through blocks") @ConfigEditorBoolean var etherDepth = EdenFeatures.etherwarpDepth
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Left Click Mode", desc = "Activation mode") @ConfigEditorDropdown(values = ["Off", "Left Click", "Left Click + Shift"]) var etherClick = EdenFeatures.etherwarpLeftClickMode
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Keybind", desc = "Toggle key") @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN) var etherKey = readKey("Dungeons.Etherwarp.Keybind")

    @ConfigEditorAccordion(id = 3)
    @ConfigOption(name = "DungeonBreaker", desc = "DungeonBreaker settings")
    var breakerGroup = false

    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "DungeonBreaker", desc = "Mining helpers") @ConfigEditorBoolean var breaker = EdenFeatures.dungeonBreakerEnabled
    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "DungeonBreaker: Prevent Mining Secrets", desc = "Protect secret blocks") @ConfigEditorBoolean var preventSecrets = EdenFeatures.dungeonPreventMiningSecrets
    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "DungeonBreaker: Insta-mine With Fatigue", desc = "Allow intended instant mining") @ConfigEditorBoolean var instaMine = EdenFeatures.dungeonInstaMineWhenFatigue
    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "DungeonBreaker: Keybind", desc = "Toggle key") @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN) var breakerKey = readKey("Dungeons.DungeonBreaker.Keybind")

    @ConfigEditorAccordion(id = 4)
    @ConfigOption(name = "Teammate Highlight", desc = "Teammate highlight settings")
    var highlightGroup = false

    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Teammate Highlight", desc = "Highlight dungeon teammates by class") @ConfigEditorBoolean var highlight = EdenFeatures.teammateHighlightEnabled
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Teammate Highlight: Keybind", desc = "Toggle key") @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN) var highlightKey = readKey("Dungeons.Teammate Highlight.Keybind")
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Archer Color", desc = "Archer highlight") @ConfigEditorColour var archer = legacy(EdenFeatures.archerColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Berserker Color", desc = "Berserker highlight") @ConfigEditorColour var berserker = legacy(EdenFeatures.berserkerColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Tank Color", desc = "Tank highlight") @ConfigEditorColour var tank = legacy(EdenFeatures.tankColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Mage Color", desc = "Mage highlight") @ConfigEditorColour var mage = legacy(EdenFeatures.mageColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Healer Color", desc = "Healer highlight") @ConfigEditorColour var healer = legacy(EdenFeatures.healerColor.argb)

    fun save() {
        bool("Dungeons.Dungeon Score Meter", scoreEnabled, true); dropdown("Dungeons.Dungeon Score Meter.Anchor", scoreAnchor); slider("Dungeons.Dungeon Score Meter.Scale", scoreScale, 50f, 200f, "%")
        dropdown("Dungeons.Dungeon Score Meter.Theme", scoreTheme); color("Dungeons.Dungeon Score Meter.Gradient 1st Color", scoreColor1); color("Dungeons.Dungeon Score Meter.Gradient 2nd Color", scoreColor2); slider("Dungeons.Dungeon Score Meter.Gradient Rotation", scoreRotation, 0f, 360f, "°")
        bool("Dungeons.Etherwarp", etherwarp, true); bool("Dungeons.Etherwarp.Show Guess", etherGuess); color("Dungeons.Etherwarp.Color", etherColor); bool("Dungeons.Etherwarp.Show when failed", etherFailed); color("Dungeons.Etherwarp.Fail Color", etherFailColor); dropdown("Dungeons.Etherwarp.Render Style", etherStyle); bool("Dungeons.Etherwarp.Use Server Position", etherServer); bool("Dungeons.Etherwarp.Full Block", etherFull); bool("Dungeons.Etherwarp.Depth", etherDepth); dropdown("Dungeons.Etherwarp.Left Click Mode", etherClick); writeKey("Dungeons.Etherwarp.Keybind", etherKey)
        bool("Dungeons.DungeonBreaker", breaker, true); bool("Dungeons.DungeonBreaker.Prevent mining secrets", preventSecrets); bool("Dungeons.DungeonBreaker.Insta-mine when fatigue", instaMine); writeKey("Dungeons.DungeonBreaker.Keybind", breakerKey)
        bool("Dungeons.Teammate Highlight", highlight, true); writeKey("Dungeons.Teammate Highlight.Keybind", highlightKey); color("Dungeons.Teammate Highlight.Archer Color", archer); color("Dungeons.Teammate Highlight.Berserker Color", berserker); color("Dungeons.Teammate Highlight.Tank Color", tank); color("Dungeons.Teammate Highlight.Mage Color", mage); color("Dungeons.Teammate Highlight.Healer Color", healer)
    }
}

class VisualsCategory {
    @ConfigEditorAccordion(id = 10)
    @ConfigOption(name = "Player Hider", desc = "Player hider settings")
    var hiderGroup = false

    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider", desc = "Hide or fade nearby players") @ConfigEditorBoolean var hider = EdenFeatures.playerHiderEnabled
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Hide Players", desc = "Hide nearby players") @ConfigEditorBoolean var hidePlayers = EdenFeatures.playerHiderHidePlayers
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Distance", desc = "Hide distance") @ConfigEditorSlider(minValue = .5f, maxValue = 10f, minStep = .1f) var hideDistance = EdenFeatures.playerHiderDistance
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Hide All", desc = "Hide all players") @ConfigEditorBoolean var hideAll = EdenFeatures.playerHiderHideAll
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Ghost Mode", desc = "Fade instead of hide") @ConfigEditorBoolean var ghost = EdenFeatures.playerHiderGhostMode
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Opacity", desc = "Ghost opacity percent") @ConfigEditorSlider(minValue = 0f, maxValue = 100f, minStep = 1f) var ghostOpacity = EdenFeatures.playerHiderGhostOpacity * 100f
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Click Through", desc = "Ignore hidden players when targeting") @ConfigEditorBoolean var clickThrough = EdenFeatures.playerHiderClickThrough
    @ConfigEditorAccordion(id = 11)
    @ConfigOption(name = "Player Size", desc = "Player size settings")
    var sizeGroup = false

    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size", desc = "Scale player models") @ConfigEditorBoolean var size = EdenFeatures.playerSizeEnabled
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: Scale All", desc = "Scale every player") @ConfigEditorBoolean var sizeAll = EdenFeatures.playerSizeScaleAllPlayers
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: X", desc = "Horizontal scale") @ConfigEditorSlider(minValue = .1f, maxValue = 3f, minStep = .1f) var sizeX = EdenFeatures.playerSizeX
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: Y", desc = "Vertical scale") @ConfigEditorSlider(minValue = -3f, maxValue = 3f, minStep = .1f) var sizeY = EdenFeatures.playerSizeY
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: Z", desc = "Depth scale") @ConfigEditorSlider(minValue = .1f, maxValue = 3f, minStep = .1f) var sizeZ = EdenFeatures.playerSizeZ
    @ConfigEditorAccordion(id = 12)
    @ConfigOption(name = "Hit Color", desc = "Hit color settings")
    var hitColorGroup = false

    @ConfigAccordionId(id = 12)
    @ConfigOption(name = "Hit Color", desc = "Tint entities when hit") @ConfigEditorBoolean var hitColorEnabled = EdenFeatures.hitColorEnabled
    @ConfigAccordionId(id = 12)
    @ConfigOption(name = "Hit Color: Color", desc = "Hit tint") @ConfigEditorColour var hitColor = legacy(EdenFeatures.hitColor.argb)
    @ConfigOption(name = "Fullbright", desc = "Keep the world fully lit") @ConfigEditorBoolean var fullbright = EdenFeatures.fullbrightEnabled

    @ConfigEditorAccordion(id = 13)
    @ConfigOption(name = "Performance HUD", desc = "Performance HUD settings")
    var performanceGroup = false

    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD", desc = "Show FPS, TPS and ping") @ConfigEditorBoolean var performance = EdenFeatures.performanceHudEnabled
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Direction", desc = "Layout direction") @ConfigEditorDropdown(values = ["Horizontal", "Vertical"]) var performanceDirection = EdenFeatures.performanceHudDirection
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Show FPS", desc = "Show frames per second") @ConfigEditorBoolean var showFps = EdenFeatures.performanceHudShowFps
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Show TPS", desc = "Show server tick rate") @ConfigEditorBoolean var showTps = EdenFeatures.performanceHudShowTps
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Show Ping", desc = "Show latency") @ConfigEditorBoolean var showPing = EdenFeatures.performanceHudShowPing
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Anchor", desc = "HUD anchor") @ConfigEditorDropdown(values = ["Top Left", "Middle Left", "Bottom Left", "Top Right", "Middle Right", "Bottom Right", "Top Middle", "Bottom Middle"]) var performanceAnchor = EdenFeatures.performanceHudAnchor
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) var performanceScale = EdenFeatures.performanceHudScale * 100f
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Edit Performance HUD", desc = "Position the performance HUD") @ConfigEditorButton(buttonText = "Open") val editPerformance = Runnable { Minecraft.getInstance().setScreen(EdenHudEditorScreen(null, EdenHudWidget.PERFORMANCE)) }
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Name Color", desc = "Label color") @ConfigEditorColour var performanceNameColor = legacy(EdenFeatures.performanceHudNameColor.argb)
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Value Color", desc = "Value color") @ConfigEditorColour var performanceValueColor = legacy(EdenFeatures.performanceHudValueColor.argb)

    @ConfigEditorAccordion(id = 14)
    @ConfigOption(name = "Render Optimizer", desc = "Render optimizer settings")
    var optimizerGroup = false

    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Render Optimizer", desc = "Hide selected visual clutter") @ConfigEditorBoolean var optimizer = EdenFeatures.renderOptimizerEnabled
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Falling Blocks", desc = "Optimizer option") @ConfigEditorBoolean var falling = EdenFeatures.renderOptimizerHideFallingBlocks
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Lightning", desc = "Optimizer option") @ConfigEditorBoolean var lightning = EdenFeatures.renderOptimizerHideLightning
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Experience Orbs", desc = "Optimizer option") @ConfigEditorBoolean var xpOrbs = EdenFeatures.renderOptimizerHideExperienceOrbs
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Death Animation", desc = "Optimizer option") @ConfigEditorBoolean var death = EdenFeatures.renderOptimizerHideDeathAnimation
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Dying Armor Stands", desc = "Optimizer option") @ConfigEditorBoolean var armorStands = EdenFeatures.renderOptimizerHideDyingArmorStands
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Explosion Particles", desc = "Optimizer option") @ConfigEditorBoolean var explosions = EdenFeatures.renderOptimizerHideExplosionParticles
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Archer Passive", desc = "Optimizer option") @ConfigEditorBoolean var archerPassive = EdenFeatures.renderOptimizerHideArcherPassive
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Healer Fairy", desc = "Optimizer option") @ConfigEditorBoolean var healerFairy = EdenFeatures.renderOptimizerHideHealerFairy
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Soul Weaver", desc = "Optimizer option") @ConfigEditorBoolean var soulWeaver = EdenFeatures.renderOptimizerHideSoulWeaver
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Tentacle Head", desc = "Optimizer option") @ConfigEditorBoolean var tentacle = EdenFeatures.renderOptimizerHideTentacleHead
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Fire Overlay", desc = "Optimizer option") @ConfigEditorBoolean var fire = EdenFeatures.renderOptimizerHideFireOverlay

    @ConfigEditorAccordion(id = 15)
    @ConfigOption(name = "Name Replace", desc = "Name replacement settings")
    var nameGroup = false

    @ConfigAccordionId(id = 15)
    @ConfigOption(name = "Name Replace", desc = "Replace your displayed name") @ConfigEditorBoolean var nameReplace = EdenFeatures.nameReplaceEnabled
    @ConfigAccordionId(id = 15)
    @ConfigOption(name = "Name Replacement", desc = "Replacement text") @ConfigEditorText var name = EdenFeatures.nameReplacement
    @ConfigAccordionId(id = 15)
    @ConfigOption(name = "Name Color", desc = "Replacement color") @ConfigEditorColour var nameColor = legacy(EdenFeatures.nameReplaceColor.argb)
    @ConfigEditorAccordion(id = 16)
    @ConfigOption(name = "Zoom", desc = "Zoom settings")
    var zoomGroup = false

    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom", desc = "Smooth zoom feature") @ConfigEditorBoolean var zoom = EdenFeatures.zoomEnabled
    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom Intensity", desc = "Zoom strength") @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f) var zoomIntensity = EdenFeatures.zoomIntensity
    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom Scrollable", desc = "Change zoom with the wheel") @ConfigEditorBoolean var zoomScrollable = EdenFeatures.zoomScrollable
    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom Keybind", desc = "Hold to zoom") @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_C) var zoomKey = readKey("Visuals.Zoom.Keybind", GLFW.GLFW_KEY_C)

    @ConfigEditorAccordion(id = 17)
    @ConfigOption(name = "Pet Overlay", desc = "Pet overlay settings")
    var petGroup = false

    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Overlay", desc = "Show the active pet HUD") @ConfigEditorBoolean var pet = EdenFeatures.petOverlayEnabled
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Type", desc = "Overlay style") @ConfigEditorDropdown(values = ["Bar", "Bar (alt)", "Circular", "Circular (alt)"]) var petType = EdenFeatures.petOverlayType
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Show Pet Item", desc = "Show held pet item") @ConfigEditorBoolean var petItem = EdenFeatures.petOverlayShowItem
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Invert Level/XP Color", desc = "Swap colors") @ConfigEditorBoolean var petInvert = EdenFeatures.petOverlayInvert
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Flip Icon Position", desc = "Move icon to other side") @ConfigEditorBoolean var petFlip = EdenFeatures.petOverlayFlip
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Anchor", desc = "HUD anchor") @ConfigEditorDropdown(values = ["Top Left", "Middle Left", "Bottom Left", "Top Right", "Middle Right", "Bottom Right", "Top Middle", "Bottom Middle"]) var petAnchor = EdenFeatures.petOverlayAnchor
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) var petScale = EdenFeatures.petOverlayScale * 100f
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Edit Pet HUD", desc = "Position the pet overlay") @ConfigEditorButton(buttonText = "Open") val editPet = Runnable { Minecraft.getInstance().setScreen(EdenHudEditorScreen(null, EdenHudWidget.PET)) }
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Theme", desc = "Overlay colors") @ConfigEditorDropdown(values = ["Pet Rarity", "Custom", "Special", "Divine", "Mythic", "Legendary", "Epic", "Rare", "Uncommon", "Common"]) var petTheme = EdenFeatures.petOverlayTheme
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Level Color", desc = "Level color") @ConfigEditorColour var petLevel = legacy(EdenFeatures.petOverlayLevelColor.argb)
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet XP Color", desc = "XP color") @ConfigEditorColour var petXp = legacy(EdenFeatures.petOverlayXpColor.argb)
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Background Color", desc = "Background color") @ConfigEditorColour var petBackground = legacy(EdenFeatures.petOverlayBackgroundColor.argb)
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Idle Pulse", desc = "Idle pulse animation") @ConfigEditorBoolean var petPulse = EdenFeatures.petOverlayIdlePulse
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Idle Hover", desc = "Idle hover animation") @ConfigEditorBoolean var petHover = EdenFeatures.petOverlayIdleHover
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Level Up Animation", desc = "Animate level ups") @ConfigEditorBoolean var petLevelUp = EdenFeatures.petOverlayLevelUpAnimation
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Level/XP Animation", desc = "Animate changing values") @ConfigEditorBoolean var petValue = EdenFeatures.petOverlayValueAnimation
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Rainbow Pet Level", desc = "Rainbow level") @ConfigEditorBoolean var petRainbowLevel = EdenFeatures.petOverlayRainbowLevel
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Rainbow Pet XP", desc = "Rainbow XP") @ConfigEditorBoolean var petRainbowXp = EdenFeatures.petOverlayRainbowXp
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Rainbow Pet Background", desc = "Rainbow background") @ConfigEditorBoolean var petRainbowBg = EdenFeatures.petOverlayRainbowBackground

    @ConfigEditorAccordion(id = 18)
    @ConfigOption(name = "Pressure Display", desc = "Pressure display settings")
    var pressureGroup = false

    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Display", desc = "Show pressure threshold") @ConfigEditorBoolean var pressure = EdenFeatures.pressureDisplayEnabled
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Show At", desc = "Threshold percent") @ConfigEditorSlider(minValue = 1f, maxValue = 99f, minStep = 1f) var pressureAt = EdenFeatures.pressureDisplayShowAt * 100f
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Anchor", desc = "HUD anchor") @ConfigEditorDropdown(values = ["Top Left", "Middle Left", "Bottom Left", "Top Right", "Middle Right", "Bottom Right", "Top Middle", "Bottom Middle"]) var pressureAnchor = EdenFeatures.pressureDisplayAnchor
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) var pressureScale = EdenFeatures.pressureDisplayScale * 100f
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Edit Pressure HUD", desc = "Position the pressure display") @ConfigEditorButton(buttonText = "Open") val editPressure = Runnable { Minecraft.getInstance().setScreen(EdenHudEditorScreen(null, EdenHudWidget.PRESSURE)) }
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Theme", desc = "Display theme") @ConfigEditorDropdown(values = ["Nighttime", "Peach"]) var pressureTheme = EdenFeatures.pressureDisplayTheme
    @ConfigEditorAccordion(id = 19)
    @ConfigOption(name = "Low HP Indicator", desc = "Low HP indicator settings")
    var lowHpGroup = false

    @ConfigAccordionId(id = 19)
    @ConfigOption(name = "Low HP Indicator", desc = "Screen warning at low health") @ConfigEditorBoolean var lowHp = EdenFeatures.lowHpIndicatorEnabled
    @ConfigAccordionId(id = 19)
    @ConfigOption(name = "Low HP Transparency", desc = "Warning opacity percent") @ConfigEditorSlider(minValue = 20f, maxValue = 100f, minStep = 1f) var lowHpAlpha = EdenFeatures.lowHpIndicatorTransparency * 100f
    @ConfigAccordionId(id = 19)
    @ConfigOption(name = "Low HP Pulse", desc = "Heartbeat animation") @ConfigEditorBoolean var lowHpPulse = EdenFeatures.lowHpIndicatorHeartbeat
    @ConfigEditorAccordion(id = 20)
    @ConfigOption(name = "Drill Fuel Meter", desc = "Drill fuel HUD settings")
    var drillGroup = false

    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Drill Fuel Meter", desc = "Show drill fuel") @ConfigEditorBoolean var drill = EdenFeatures.drillFuelMeterEnabled
    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Drill Anchor", desc = "HUD anchor") @ConfigEditorDropdown(values = ["Top Left", "Middle Left", "Bottom Left", "Top Right", "Middle Right", "Bottom Right", "Top Middle", "Bottom Middle"]) var drillAnchor = EdenFeatures.drillFuelMeterAnchor
    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Drill Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) var drillScale = EdenFeatures.drillFuelMeterScale * 100f
    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Edit Drill HUD", desc = "Position the drill meter") @ConfigEditorButton(buttonText = "Open") val editDrill = Runnable { Minecraft.getInstance().setScreen(EdenHudEditorScreen(null, EdenHudWidget.DRILL_FUEL)) }
    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Drill Theme", desc = "Meter theme") @ConfigEditorDropdown(values = ["Biofuel", "Mithril"]) var drillTheme = EdenFeatures.drillFuelMeterTheme
    @ConfigEditorAccordion(id = 21)
    @ConfigOption(name = "Action Bar Cleanup", desc = "Action bar cleanup settings")
    var cleanupGroup = false

    @ConfigAccordionId(id = 21)
    @ConfigOption(name = "Action Bar Cleanup", desc = "Remove duplicated HUD data") @ConfigEditorBoolean var cleanup = EdenFeatures.actionBarCleanupEnabled
    @ConfigAccordionId(id = 21)
    @ConfigOption(name = "Hide Pressure in Action Bar", desc = "Remove pressure text") @ConfigEditorBoolean var cleanupPressure = EdenFeatures.hidePressureInActionBar
    @ConfigAccordionId(id = 21)
    @ConfigOption(name = "Hide Drill Fuel in Action Bar", desc = "Remove fuel text") @ConfigEditorBoolean var cleanupFuel = EdenFeatures.hideDrillFuelInActionBar

    fun save() {
        bool("Visuals.Player Hider", hider, true); bool("Visuals.Player Hider.Hide Players", hidePlayers); slider("Visuals.Player Hider.Distance", hideDistance, .5f, 10f); bool("Visuals.Player Hider.Hide All", hideAll); bool("Visuals.Player Hider.Ghost Mode", ghost); slider("Visuals.Player Hider.Opacity", ghostOpacity, 0f, 100f, "%"); bool("Visuals.Player Hider.Click Through Players", clickThrough)
        bool("Visuals.Player Size", size, true); bool("Visuals.Player Size.Scale All Players", sizeAll); slider("Visuals.Player Size.X Scale", sizeX, .1f, 3f); slider("Visuals.Player Size.Y Scale", sizeY, -3f, 3f); slider("Visuals.Player Size.Z Scale", sizeZ, .1f, 3f)
        bool("Visuals.Hit Color", hitColorEnabled, true); color("Visuals.Hit Color.Color", hitColor); bool("Visuals.Fullbright", fullbright, true)
        bool("Visuals.Performance HUD", performance, true); dropdown("Visuals.Performance HUD.Direction", performanceDirection); bool("Visuals.Performance HUD.Show FPS", showFps); bool("Visuals.Performance HUD.Show TPS", showTps); bool("Visuals.Performance HUD.Show Ping", showPing); dropdown("Visuals.Performance HUD.Anchor", performanceAnchor); slider("Visuals.Performance HUD.Scale", performanceScale, 50f, 200f, "%"); color("Visuals.Performance HUD.Name Color", performanceNameColor); color("Visuals.Performance HUD.Value Color", performanceValueColor)
        bool("Visuals.Render Optimizer", optimizer, true); bool("Visuals.Render Optimizer.Hide Falling Blocks", falling); bool("Visuals.Render Optimizer.Hide Lightning", lightning); bool("Visuals.Render Optimizer.Hide Experience Orbs", xpOrbs); bool("Visuals.Render Optimizer.Hide Death Animation", death); bool("Visuals.Render Optimizer.Hide Dying Armor Stands", armorStands); bool("Visuals.Render Optimizer.Hide Explosion Particles", explosions); bool("Visuals.Render Optimizer.Hide Archer Passive", archerPassive); bool("Visuals.Render Optimizer.Hide Healer Fairy", healerFairy); bool("Visuals.Render Optimizer.Hide Soul Weaver", soulWeaver); bool("Visuals.Render Optimizer.Hide Tentacle Head", tentacle); bool("Visuals.Render Optimizer.Hide Fire Overlay", fire)
        bool("Visuals.Name Replace", nameReplace, true); text("Visuals.Name Replace.Replacement", name); color("Visuals.Name Replace.Color", nameColor); bool("Visuals.Zoom", zoom, true); slider("Visuals.Zoom.Intensity", zoomIntensity.toFloat(), 1f, 10f); bool("Visuals.Zoom.Scrollable", zoomScrollable); writeKey("Visuals.Zoom.Keybind", zoomKey)
        bool("Visuals.Pet Overlay", pet, true); dropdown("Visuals.Pet Overlay.Type", petType); bool("Visuals.Pet Overlay.Show Pet Item", petItem); bool("Visuals.Pet Overlay.Invert Level/XP Color", petInvert); bool("Visuals.Pet Overlay.Flip Icon Position", petFlip); dropdown("Visuals.Pet Overlay.Anchor", petAnchor); slider("Visuals.Pet Overlay.Scale", petScale, 50f, 200f, "%"); dropdown("Visuals.Pet Overlay.Theme", petTheme); color("Visuals.Pet Overlay.Level Color", petLevel); color("Visuals.Pet Overlay.XP Color", petXp); color("Visuals.Pet Overlay.Background Color", petBackground); bool("Visuals.Pet Overlay.Idle Pulse", petPulse); bool("Visuals.Pet Overlay.Idle Hover", petHover); bool("Visuals.Pet Overlay.Level Up Animation", petLevelUp); bool("Visuals.Pet Overlay.Level/XP Animation", petValue); bool("Visuals.Pet Overlay.Rainbow Level", petRainbowLevel); bool("Visuals.Pet Overlay.Rainbow XP", petRainbowXp); bool("Visuals.Pet Overlay.Rainbow Background", petRainbowBg)
        bool("Visuals.Pressure Display", pressure, true); slider("Visuals.Pressure Display.Show At", pressureAt, 1f, 99f, "%"); dropdown("Visuals.Pressure Display.Anchor", pressureAnchor); slider("Visuals.Pressure Display.Scale", pressureScale, 50f, 200f, "%"); dropdown("Visuals.Pressure Display.Theme", pressureTheme)
        bool("Visuals.Low HP Indicator", lowHp, true); slider("Visuals.Low HP Indicator.Transparency", lowHpAlpha, 20f, 100f, "%"); bool("Visuals.Low HP Indicator.Pulse Animation", lowHpPulse)
        bool("Visuals.Drill Fuel Meter", drill, true); dropdown("Visuals.Drill Fuel Meter.Anchor", drillAnchor); slider("Visuals.Drill Fuel Meter.Scale", drillScale, 50f, 200f, "%"); dropdown("Visuals.Drill Fuel Meter.Theme", drillTheme); bool("Visuals.Action Bar Cleanup", cleanup, true); bool("Visuals.Action Bar Cleanup.Hide Pressure", cleanupPressure); bool("Visuals.Action Bar Cleanup.Hide Drill Fuel", cleanupFuel)
    }
}

class MiscCategory {
    @ConfigOption(name = "Diana QoL", desc = "Diana quality-of-life features") @ConfigEditorBoolean var diana = EdenFeatures.dianaQolEnabled
    @ConfigOption(name = "Golden Fish CI", desc = "Golden Fish cast indicator") @ConfigEditorBoolean var fish = EdenFeatures.goldenFishCiEnabled
    @ConfigOption(name = "Leap Frog", desc = "Leap Frog helper") @ConfigEditorBoolean var leap = EdenFeatures.leapFrogEnabled
    @ConfigOption(name = "Smart Term AC", desc = "Smart terminal autocorrect") @ConfigEditorBoolean var term = EdenFeatures.smartTermAcEnabled
    @ConfigOption(name = "Infinite Chat", desc = "Keep more chat history") @ConfigEditorBoolean var chat = EdenFeatures.infiniteChatEnabled
    @ConfigOption(name = "Missing Enchants", desc = "Highlight missing enchantments") @ConfigEditorBoolean var enchants = EdenFeatures.missingEnchantsEnabled
    @ConfigOption(name = "Compact Pet Level", desc = "Compact pet level text") @ConfigEditorBoolean var petLevel = EdenFeatures.compactPetLevelEnabled
    @ConfigEditorAccordion(id = 30)
    @ConfigOption(name = "Reminder", desc = "Reminder settings")
    var reminderGroup = false

    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder", desc = "Periodic SkyBlock reminders") @ConfigEditorBoolean var reminder = EdenFeatures.reminderEnabled
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder Warning Duration", desc = "Seconds on screen") @ConfigEditorSlider(minValue = 1f, maxValue = 30f, minStep = 1f) var duration = EdenFeatures.reminderWarningDuration
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder Warning Scale", desc = "Warning scale") @ConfigEditorSlider(minValue = .5f, maxValue = 2.5f, minStep = .1f) var scale = EdenFeatures.reminderWarningScale
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder Chat Command Button", desc = "Show a command button") @ConfigEditorBoolean var commandButton = EdenFeatures.reminderChatButton

    private val rules = EdenFeatures.reminderRules
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 1 Enabled", desc = "Enable this reminder") @ConfigEditorBoolean var r1e = rules[0].enabled
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 1 Name", desc = "Display name") @ConfigEditorText var r1n = rules[0].name
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 1 Days", desc = "Example: 7, 14 or 29-31") @ConfigEditorText var r1d = rules[0].days
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 1 Command", desc = "Command including slash") @ConfigEditorText var r1c = rules[0].command
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 2 Enabled", desc = "Enable this reminder") @ConfigEditorBoolean var r2e = rules[1].enabled
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 2 Name", desc = "Display name") @ConfigEditorText var r2n = rules[1].name
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 2 Days", desc = "Example: 7, 14 or 29-31") @ConfigEditorText var r2d = rules[1].days
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 2 Command", desc = "Command including slash") @ConfigEditorText var r2c = rules[1].command
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 3 Enabled", desc = "Enable this reminder") @ConfigEditorBoolean var r3e = rules[2].enabled
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 3 Name", desc = "Display name") @ConfigEditorText var r3n = rules[2].name
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 3 Days", desc = "Example: 7, 14 or 29-31") @ConfigEditorText var r3d = rules[2].days
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 3 Command", desc = "Command including slash") @ConfigEditorText var r3c = rules[2].command
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 4 Enabled", desc = "Enable this reminder") @ConfigEditorBoolean var r4e = rules[3].enabled
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 4 Name", desc = "Display name") @ConfigEditorText var r4n = rules[3].name
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 4 Days", desc = "Example: 7, 14 or 29-31") @ConfigEditorText var r4d = rules[3].days
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 4 Command", desc = "Command including slash") @ConfigEditorText var r4c = rules[3].command
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 5 Enabled", desc = "Enable this reminder") @ConfigEditorBoolean var r5e = rules[4].enabled
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 5 Name", desc = "Display name") @ConfigEditorText var r5n = rules[4].name
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 5 Days", desc = "Example: 7, 14 or 29-31") @ConfigEditorText var r5d = rules[4].days
    @ConfigAccordionId(id = 30)
    @ConfigOption(name = "Reminder 5 Command", desc = "Command including slash") @ConfigEditorText var r5c = rules[4].command

    fun save() {
        bool("Misc.Diana QoL", diana, true); bool("Misc.Golden Fish CI", fish, true); bool("Misc.Leap Frog", leap, true); bool("Misc.Smart Term AC", term, true); bool("Misc.Infinite Chat", chat, true); bool("Misc.Missing Enchants", enchants, true); bool("Misc.Compact Pet Level", petLevel, true)
        bool("Misc.Reminder", reminder, true); slider("Misc.Reminder.Warning Duration", duration.toFloat(), 1f, 30f); slider("Misc.Reminder.Warning Scale", scale, .5f, 2.5f); bool("Misc.Reminder.Chat Command Button", commandButton)
        saveRule(1, r1e, r1n, r1d, r1c); saveRule(2, r2e, r2n, r2d, r2c); saveRule(3, r3e, r3n, r3d, r3c); saveRule(4, r4e, r4n, r4d, r4c); saveRule(5, r5e, r5n, r5d, r5c)
    }

    private fun saveRule(n: Int, enabled: Boolean, name: String, days: String, command: String) {
        bool("Misc.Reminder.Reminder $n Enabled", enabled); text("Misc.Reminder.Reminder $n Name", name); text("Misc.Reminder.Reminder $n Days", days); text("Misc.Reminder.Reminder $n Command", command)
    }
}

private fun bool(path: String, value: Boolean, module: Boolean = false) = EdenConfig.updateEntry(path) { if (module) it.enabled = value else it.switchValue = value }
private fun dropdown(path: String, value: Int) = EdenConfig.updateEntry(path) { it.selected = value }
private fun text(path: String, value: String) = EdenConfig.updateEntry(path) { it.value = value }
private fun slider(path: String, value: Float, min: Float, max: Float, suffix: String = "") = EdenConfig.updateEntry(path) {
    it.value = if (value % 1f == 0f) "${value.roundToInt()}$suffix" else "$value$suffix"
    it.sliderPercentage = ((value - min) / (max - min)).coerceIn(0f, 1f)
}
private fun color(path: String, value: String) = EdenConfig.updateEntry(path) { it.color = ChromaColour.specialToSimpleRGB(value) }
private fun legacy(argb: Int): String = ChromaColour.special(0, (argb ushr 24) and 255, argb and 0xFFFFFF)
private fun readKey(path: String, fallback: Int = GLFW.GLFW_KEY_UNKNOWN): Int {
    val name = EdenConfig.entry(path)?.keyName ?: return fallback
    return runCatching { com.mojang.blaze3d.platform.InputConstants.getKey(name).value }.getOrDefault(fallback)
}
private fun writeKey(path: String, value: Int) = EdenConfig.updateEntry(path) {
    if (value == GLFW.GLFW_KEY_UNKNOWN) { it.keyName = null; it.value = "n/a" }
    else { it.keyName = "key.keyboard.${GLFW.glfwGetKeyName(value, 0)?.lowercase() ?: value}"; it.value = GLFW.glfwGetKeyName(value, 0)?.uppercase() ?: value.toString() }
}
