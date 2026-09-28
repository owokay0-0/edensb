package dev.eden.client

import com.google.gson.annotations.Expose
import dev.eden.client.feature.EdenFeatures
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.*
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.network.chat.Component
import java.net.URI
import kotlin.math.roundToInt

private const val GLFW_KEY_C = 67

object EdenMoulConfig {
    private var config = EdenConfigRoot()

    fun reload() {
        config = EdenConfigRoot()
        config.saveRunnables.add(Runnable { config.writeBack() })
    }

    fun open() {
        reload()
		val currentConfig = config
		val processor = MoulConfigProcessor(currentConfig)
		BuiltinMoulConfigGuis.addProcessors(processor)
		processor.registerConfigEditor(ConfigEditorChatShortcuts::class.java) { option, _ ->
			ChatShortcutsEditor(option)
		}
		ConfigProcessorDriver(processor).processConfig(currentConfig)
		val editor = MoulConfigEditor(processor)
		val context = GuiContext(GuiElementComponent(editor))
		val screen = object : MoulConfigScreenComponent(Component.empty(), context, null) {
			override fun added() {
				super.added()
				// MoulConfig 4.7.2 predates Minecraft 26.3's explicit SDL text-input lifecycle.
				Minecraft.getInstance().textInputManager().startTextInput(this)
			}

			override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
				return editor.mouseInput(
					event.x().toInt(),
					event.y().toInt(),
					MouseEvent.Click(MoulConfigInputCompat.toGlfwMouseButton(event.button()), true),
				)
			}

			override fun mouseReleased(event: MouseButtonEvent): Boolean {
				return editor.mouseInput(
					event.x().toInt(),
					event.y().toInt(),
					MouseEvent.Click(MoulConfigInputCompat.toGlfwMouseButton(event.button()), false),
				)
			}

			override fun charTyped(event: CharacterEvent): Boolean {
				return editor.keyboardInput(KeyboardEvent.CharTyped(event.codepoint().toChar()))
			}

			override fun keyPressed(event: KeyEvent): Boolean {
				val keycode = MoulConfigInputCompat.toGlfwKeycode(event.keycode(), event.key())
				if (keycode == 256) {
					onClose()
					return true
				}
				return editor.keyboardInput(
					KeyboardEvent.KeyPressed(
						keycode,
						MoulConfigInputCompat.toGlfwKey(event.key()),
						true,
					)
				)
			}

			override fun keyReleased(event: KeyEvent): Boolean {
				return editor.keyboardInput(
					KeyboardEvent.KeyPressed(
						MoulConfigInputCompat.toGlfwKeycode(event.keycode(), event.key()),
						MoulConfigInputCompat.toGlfwKey(event.key()),
						false,
					)
				)
			}

			override fun removed() {
				Minecraft.getInstance().textInputManager().stopTextInput(this)
				currentConfig.saveNow()
				super.removed()
			}
		}
		Minecraft.getInstance().gui.setScreen(screen)
    }
}

class EdenConfigRoot : Config() {
    @Expose
    @Category(name = "General", desc = "Eden shortcuts and links")
    @JvmField
    var general = GeneralCategory()

    @Expose
    @Category(name = "Dungeons", desc = "Dungeon helpers and overlays")
    @JvmField
    var dungeons = DungeonsCategory()

    @Expose
    @Category(name = "Visuals", desc = "Rendering, HUDs and visual customization")
    @JvmField
    var visuals = VisualsCategory()

    @Expose
    @Category(name = "Misc", desc = "Quality-of-life features")
    @JvmField
    var misc = MiscCategory()

    override fun getTitle(): StructuredText = StructuredText.of("Eden")
    override fun executeRunnable(runnableId: Int) {
        writeBack()
    }

    fun writeBack() {
        EdenConfig.batchUpdate {
            dungeons.save()
            visuals.save()
            misc.save()
        }
        EdenFeatures.loadFromConfig()
    }
}

class GeneralCategory {
    @ConfigOption(name = "HUD Editor", desc = "Move and resize every Eden HUD") @ConfigEditorButton(buttonText = "Edit")
    @Transient @JvmField
    val openHud = Runnable {
        val client = Minecraft.getInstance()
        client.gui.setScreen(EdenHudEditorScreen(client.gui.screen()))
    }

    @ConfigOption(name = "GitHub", desc = "Open Eden's Github") @ConfigEditorButton(buttonText = "Open")
    @Transient @JvmField
    val openGitHub = Runnable { openEdenLink("https://github.com/owokay0-0/edensb") }

    @ConfigOption(name = "Modrinth", desc = "Open Eden's Modrinth page") @ConfigEditorButton(buttonText = "Open")
    @Transient @JvmField
    val openModrinth = Runnable { openEdenLink("https://modrinth.com/mod/edensb") }

}

class DungeonsCategory {
    @ConfigEditorAccordion(id = 1)
    @ConfigOption(name = "Dungeon Score Meter", desc = "Dungeon score HUD settings")
    @Transient @JvmField
    var scoreGroup = false

    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Dungeon Score Meter", desc = "Show the current dungeon score on the HUD") @ConfigEditorBoolean
    @Expose @JvmField
    var scoreEnabled = EdenFeatures.dungeonScoreMeterEnabled
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f)
    @Expose @JvmField
    var scoreScale = EdenFeatures.dungeonScoreMeterScale * 100f
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Edit Score HUD", desc = "Position the score meter") @ConfigEditorButton(buttonText = "Open")
    @Transient @JvmField
    val editScore = Runnable { Minecraft.getInstance().gui.setScreen(EdenHudEditorScreen(null, EdenHudWidget.DUNGEON_SCORE)) }
    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Score Color", desc = "Number color") @ConfigEditorColour
    @Expose @JvmField
    var scoreColor = legacy(EdenFeatures.dungeonScoreColor.argb)

    @ConfigEditorAccordion(id = 2)
    @ConfigOption(name = "Etherwarp", desc = "Etherwarp settings")
    @Transient @JvmField
    var etherwarpGroup = false

    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp", desc = "Etherwarp targeting helpers") @ConfigEditorBoolean
    @Expose @JvmField
    var etherwarp = EdenFeatures.etherwarpEnabled
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Show Guess", desc = "Show the predicted target") @ConfigEditorBoolean @Expose @JvmField var etherGuess = EdenFeatures.etherwarpShowGuess
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Color", desc = "Target color") @ConfigEditorColour @Expose @JvmField var etherColor = legacy(EdenFeatures.etherwarpColor.argb)
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Show Failed", desc = "Show failed targets") @ConfigEditorBoolean @Expose @JvmField var etherFailed = EdenFeatures.etherwarpShowFailed
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Fail Color", desc = "Failed target color") @ConfigEditorColour @Expose @JvmField var etherFailColor = legacy(EdenFeatures.etherwarpFailColor.argb)
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Render Style", desc = "Target box style") @ConfigEditorDropdown(values = ["Filled", "Outline", "Filled Outline"]) @Expose @JvmField var etherStyle = EdenFeatures.etherwarpRenderStyle
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Use Server Position", desc = "Use server position") @ConfigEditorBoolean @Expose @JvmField var etherServer = EdenFeatures.etherwarpUseServerPosition
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Full Block", desc = "Draw a full block") @ConfigEditorBoolean @Expose @JvmField var etherFull = EdenFeatures.etherwarpFullBlock
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Depth", desc = "Render through blocks") @ConfigEditorBoolean @Expose @JvmField var etherDepth = EdenFeatures.etherwarpDepth
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Left Click Mode", desc = "Activation mode") @ConfigEditorDropdown(values = ["Off", "Left Click", "Left Click + Shift"]) @Expose @JvmField var etherClick = EdenFeatures.etherwarpLeftClickMode
    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Etherwarp: Keybind", desc = "Toggle key") @ConfigEditorKeybind(defaultKey = -1) @Expose @JvmField var etherKey = readKey("Dungeons.Etherwarp.Keybind")

    @ConfigEditorAccordion(id = 3)
    @ConfigOption(name = "DungeonBreaker", desc = "DungeonBreaker settings")
    @Transient @JvmField
    var breakerGroup = false

    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "DungeonBreaker", desc = "Master toggle") @ConfigEditorBoolean @Expose @JvmField var breaker = EdenFeatures.dungeonBreakerEnabled
    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "Prevent mining secrets", desc = "Don't mine secrets (DT)") @ConfigEditorBoolean @Expose @JvmField var preventSecrets = EdenFeatures.dungeonPreventMiningSecrets
    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "Insta mine with fatigue", desc = "Remove slow mining when fatigue client sided to make game feel better") @ConfigEditorBoolean @Expose @JvmField var instaMine = EdenFeatures.dungeonInstaMineWhenFatigue
    @ConfigAccordionId(id = 3)
    @ConfigOption(name = "DungeonBreaker: Keybind", desc = "Toggle key") @ConfigEditorKeybind(defaultKey = -1) @Expose @JvmField var breakerKey = readKey("Dungeons.DungeonBreaker.Keybind")

    @ConfigEditorAccordion(id = 4)
    @ConfigOption(name = "Teammate Highlight", desc = "Teammate highlight settings")
    @Transient @JvmField
    var highlightGroup = false

    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Teammate Highlight", desc = "Highlight dungeon teammates by class") @ConfigEditorBoolean @Expose @JvmField var highlight = EdenFeatures.teammateHighlightEnabled
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Teammate Highlight: Keybind", desc = "Toggle key") @ConfigEditorKeybind(defaultKey = -1) @Expose @JvmField var highlightKey = readKey("Dungeons.Teammate Highlight.Keybind")
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Archer Color", desc = "Archer highlight") @ConfigEditorColour @Expose @JvmField var archer = legacy(EdenFeatures.archerColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Berserker Color", desc = "Berserker highlight") @ConfigEditorColour @Expose @JvmField var berserker = legacy(EdenFeatures.berserkerColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Tank Color", desc = "Tank highlight") @ConfigEditorColour @Expose @JvmField var tank = legacy(EdenFeatures.tankColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Mage Color", desc = "Mage highlight") @ConfigEditorColour @Expose @JvmField var mage = legacy(EdenFeatures.mageColor.argb)
    @ConfigAccordionId(id = 4)
    @ConfigOption(name = "Healer Color", desc = "Healer highlight") @ConfigEditorColour @Expose @JvmField var healer = legacy(EdenFeatures.healerColor.argb)

    @ConfigEditorAccordion(id = 5)
    @ConfigOption(name = "Shitter Alert", desc = "Warn when a dungeon teammate dies or leaves")
    @Transient @JvmField
    var shitterGroup = false

    @ConfigAccordionId(id = 5)
    @ConfigOption(name = "Shitter Alert", desc = "Pop up NOT 4/4 when the team loses someone") @ConfigEditorBoolean @Expose @JvmField var shitter = EdenFeatures.shitterAlertEnabled
    @ConfigAccordionId(id = 5)
    @ConfigOption(name = "Shitter Alert Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) @Expose @JvmField var shitterScale = EdenFeatures.shitterAlertScale * 100f
    @ConfigAccordionId(id = 5)
    @ConfigOption(name = "Edit Shitter Alert HUD", desc = "Position and resize the alert") @ConfigEditorButton(buttonText = "Open")
    @Transient @JvmField
    val editShitter = Runnable { Minecraft.getInstance().gui.setScreen(EdenHudEditorScreen(null, EdenHudWidget.SHITTER_ALERT)) }

    fun save() {
        bool("Dungeons.Dungeon Score Meter", scoreEnabled, true); slider("Dungeons.Dungeon Score Meter.Scale", scoreScale, 50f, 200f, "%"); color("Dungeons.Dungeon Score Meter.Color", scoreColor)
        bool("Dungeons.Etherwarp", etherwarp, true); bool("Dungeons.Etherwarp.Show Guess", etherGuess); color("Dungeons.Etherwarp.Color", etherColor); bool("Dungeons.Etherwarp.Show when failed", etherFailed); color("Dungeons.Etherwarp.Fail Color", etherFailColor); dropdown("Dungeons.Etherwarp.Render Style", etherStyle); bool("Dungeons.Etherwarp.Use Server Position", etherServer); bool("Dungeons.Etherwarp.Full Block", etherFull); bool("Dungeons.Etherwarp.Depth", etherDepth); dropdown("Dungeons.Etherwarp.Left Click Mode", etherClick); writeKey("Dungeons.Etherwarp.Keybind", etherKey)
        bool("Dungeons.DungeonBreaker", breaker, true); bool("Dungeons.DungeonBreaker.Prevent mining secrets", preventSecrets); bool("Dungeons.DungeonBreaker.Insta-mine when fatigue", instaMine); writeKey("Dungeons.DungeonBreaker.Keybind", breakerKey)
        bool("Dungeons.Teammate Highlight", highlight, true); writeKey("Dungeons.Teammate Highlight.Keybind", highlightKey); color("Dungeons.Teammate Highlight.Archer Color", archer); color("Dungeons.Teammate Highlight.Berserker Color", berserker); color("Dungeons.Teammate Highlight.Tank Color", tank); color("Dungeons.Teammate Highlight.Mage Color", mage); color("Dungeons.Teammate Highlight.Healer Color", healer)
        bool("Dungeons.Shitter Alert", shitter, true); slider("Dungeons.Shitter Alert.Scale", shitterScale, 50f, 200f, "%")
    }
}

class VisualsCategory {
    @ConfigOption(name = "Fullbright", desc = "Keep the world fully lit") @ConfigEditorBoolean @Expose @JvmField var fullbright = EdenFeatures.fullbrightEnabled

    @ConfigEditorAccordion(id = 10)
    @ConfigOption(name = "Player Hider", desc = "Player hider settings")
    @Transient @JvmField
    var hiderGroup = false

    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider", desc = "Hide or fade nearby players") @ConfigEditorBoolean @Expose @JvmField var hider = EdenFeatures.playerHiderEnabled
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Hide Players", desc = "Hide nearby players") @ConfigEditorBoolean @Expose @JvmField var hidePlayers = EdenFeatures.playerHiderHidePlayers
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Distance", desc = "Hide distance") @ConfigEditorSlider(minValue = .5f, maxValue = 10f, minStep = .1f) @Expose @JvmField var hideDistance = EdenFeatures.playerHiderDistance
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Hide All", desc = "Hide all players") @ConfigEditorBoolean @Expose @JvmField var hideAll = EdenFeatures.playerHiderHideAll
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Ghost Mode", desc = "Fade instead of hide") @ConfigEditorBoolean @Expose @JvmField var ghost = EdenFeatures.playerHiderGhostMode
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Opacity", desc = "Ghost opacity percent") @ConfigEditorSlider(minValue = 0f, maxValue = 100f, minStep = 1f) @Expose @JvmField var ghostOpacity = EdenFeatures.playerHiderGhostOpacity * 100f
    @ConfigAccordionId(id = 10)
    @ConfigOption(name = "Player Hider: Click Through", desc = "Ignore hidden players when targeting") @ConfigEditorBoolean @Expose @JvmField var clickThrough = EdenFeatures.playerHiderClickThrough
    @ConfigEditorAccordion(id = 11)
    @ConfigOption(name = "Player Size", desc = "Player size settings")
    @Transient @JvmField
    var sizeGroup = false

    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size", desc = "Scale player models") @ConfigEditorBoolean @Expose @JvmField var size = EdenFeatures.playerSizeEnabled
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: Scale All", desc = "Scale every player") @ConfigEditorBoolean @Expose @JvmField var sizeAll = EdenFeatures.playerSizeScaleAllPlayers
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: X", desc = "Horizontal scale") @ConfigEditorSlider(minValue = .1f, maxValue = 3f, minStep = .1f) @Expose @JvmField var sizeX = EdenFeatures.playerSizeX
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: Y", desc = "Vertical scale") @ConfigEditorSlider(minValue = -3f, maxValue = 3f, minStep = .1f) @Expose @JvmField var sizeY = EdenFeatures.playerSizeY
    @ConfigAccordionId(id = 11)
    @ConfigOption(name = "Player Size: Z", desc = "Depth scale") @ConfigEditorSlider(minValue = .1f, maxValue = 3f, minStep = .1f) @Expose @JvmField var sizeZ = EdenFeatures.playerSizeZ
    @ConfigEditorAccordion(id = 12)
    @ConfigOption(name = "Hit Color", desc = "Hit color settings")
    @Transient @JvmField
    var hitColorGroup = false

    @ConfigAccordionId(id = 12)
    @ConfigOption(name = "Hit Color", desc = "Tint entities when hit") @ConfigEditorBoolean @Expose @JvmField var hitColorEnabled = EdenFeatures.hitColorEnabled
    @ConfigAccordionId(id = 12)
    @ConfigOption(name = "Hit Color: Color", desc = "Hit tint") @ConfigEditorColour @Expose @JvmField var hitColor = legacy(EdenFeatures.hitColor.argb)
    @ConfigEditorAccordion(id = 13)
    @ConfigOption(name = "Performance HUD", desc = "Performance HUD settings")
    @Transient @JvmField
    var performanceGroup = false

    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD", desc = "Show FPS, TPS and ping") @ConfigEditorBoolean @Expose @JvmField var performance = EdenFeatures.performanceHudEnabled
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Show FPS", desc = "Show frames per second") @ConfigEditorBoolean @Expose @JvmField var showFps = EdenFeatures.performanceHudShowFps
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Show TPS", desc = "Show server tick rate") @ConfigEditorBoolean @Expose @JvmField var showTps = EdenFeatures.performanceHudShowTps
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Show Ping", desc = "Show latency") @ConfigEditorBoolean @Expose @JvmField var showPing = EdenFeatures.performanceHudShowPing
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "FPS Graph", desc = "Show the simple FPS graph next to FPS") @ConfigEditorBoolean @Expose @JvmField var showFpsGraph = EdenFeatures.performanceHudShowFpsGraph
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "TPS Graph", desc = "Show the simple TPS graph next to TPS") @ConfigEditorBoolean @Expose @JvmField var showTpsGraph = EdenFeatures.performanceHudShowTpsGraph
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Ping Graph", desc = "Show the simple ping graph next to ping") @ConfigEditorBoolean @Expose @JvmField var showPingGraph = EdenFeatures.performanceHudShowPingGraph
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Performance HUD: Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) @Expose @JvmField var performanceScale = EdenFeatures.performanceHudScale * 100f
    @ConfigAccordionId(id = 13)
    @ConfigOption(name = "Edit Performance HUD", desc = "Position the performance HUD") @ConfigEditorButton(buttonText = "Open") @Transient @JvmField val editPerformance = Runnable { Minecraft.getInstance().gui.setScreen(EdenHudEditorScreen(null, EdenHudWidget.PERFORMANCE)) }
    @ConfigEditorAccordion(id = 14)
    @ConfigOption(name = "Render Optimizer", desc = "Render optimizer settings")
    @Transient @JvmField
    var optimizerGroup = false

    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Render Optimizer", desc = "Hide selected visual clutter") @ConfigEditorBoolean @Expose @JvmField var optimizer = EdenFeatures.renderOptimizerEnabled
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Falling Blocks", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var falling = EdenFeatures.renderOptimizerHideFallingBlocks
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Lightning", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var lightning = EdenFeatures.renderOptimizerHideLightning
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Experience Orbs", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var xpOrbs = EdenFeatures.renderOptimizerHideExperienceOrbs
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Death Animation", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var death = EdenFeatures.renderOptimizerHideDeathAnimation
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Dying Armor Stands", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var armorStands = EdenFeatures.renderOptimizerHideDyingArmorStands
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Explosion Particles", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var explosions = EdenFeatures.renderOptimizerHideExplosionParticles
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Archer Passive", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var archerPassive = EdenFeatures.renderOptimizerHideArcherPassive
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Healer Fairy", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var healerFairy = EdenFeatures.renderOptimizerHideHealerFairy
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Soul Weaver", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var soulWeaver = EdenFeatures.renderOptimizerHideSoulWeaver
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Tentacle Head", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var tentacle = EdenFeatures.renderOptimizerHideTentacleHead
    @ConfigAccordionId(id = 14)
    @ConfigOption(name = "Hide Fire Overlay", desc = "Optimizer option") @ConfigEditorBoolean @Expose @JvmField var fire = EdenFeatures.renderOptimizerHideFireOverlay

    @ConfigEditorAccordion(id = 15)
    @ConfigOption(name = "Name Replace", desc = "Name replacement settings")
    @Transient @JvmField
    var nameGroup = false

    @ConfigAccordionId(id = 15)
    @ConfigOption(name = "Name Replace", desc = "Replace your displayed name") @ConfigEditorBoolean @Expose @JvmField var nameReplace = EdenFeatures.nameReplaceEnabled
    @ConfigAccordionId(id = 15)
    @ConfigOption(name = "Name Replacement", desc = "Replacement text") @ConfigEditorText @Expose @JvmField var name = EdenFeatures.nameReplacement
    @ConfigAccordionId(id = 15)
    @ConfigOption(name = "Name Color", desc = "Replacement color") @ConfigEditorColour @Expose @JvmField var nameColor = legacy(EdenFeatures.nameReplaceColor.argb)
    @ConfigEditorAccordion(id = 16)
    @ConfigOption(name = "Zoom", desc = "Zoom settings")
    @Transient @JvmField
    var zoomGroup = false

    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom", desc = "Smooth zoom feature") @ConfigEditorBoolean @Expose @JvmField var zoom = EdenFeatures.zoomEnabled
    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom Intensity", desc = "Zoom strength") @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f) @Expose @JvmField var zoomIntensity = EdenFeatures.zoomIntensity
    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom Scrollable", desc = "Change zoom with the wheel") @ConfigEditorBoolean @Expose @JvmField var zoomScrollable = EdenFeatures.zoomScrollable
    @ConfigAccordionId(id = 16)
    @ConfigOption(name = "Zoom Keybind", desc = "Hold to zoom") @ConfigEditorKeybind(defaultKey = GLFW_KEY_C) @Expose @JvmField var zoomKey = readKey("Visuals.Zoom.Keybind", GLFW_KEY_C)

    @ConfigEditorAccordion(id = 17)
    @ConfigOption(name = "Pet Display", desc = "Current pet level text")
    @Transient @JvmField
    var petGroup = false

    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Display", desc = "Show PET LVL followed by the active pet level") @ConfigEditorBoolean @Expose @JvmField var pet = EdenFeatures.petOverlayEnabled
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Pet Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) @Expose @JvmField var petScale = EdenFeatures.petOverlayScale * 100f
    @ConfigAccordionId(id = 17)
    @ConfigOption(name = "Edit Pet HUD", desc = "Position the pet level text") @ConfigEditorButton(buttonText = "Open") @Transient @JvmField val editPet = Runnable { Minecraft.getInstance().gui.setScreen(EdenHudEditorScreen(null, EdenHudWidget.PET)) }

    @ConfigEditorAccordion(id = 18)
    @ConfigOption(name = "Pressure Display", desc = "Pressure display settings")
    @Transient @JvmField
    var pressureGroup = false

    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Display", desc = "Show pressure threshold") @ConfigEditorBoolean @Expose @JvmField var pressure = EdenFeatures.pressureDisplayEnabled
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Show At", desc = "Threshold percent") @ConfigEditorSlider(minValue = 1f, maxValue = 99f, minStep = 1f) @Expose @JvmField var pressureAt = EdenFeatures.pressureDisplayShowAt * 100f
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) @Expose @JvmField var pressureScale = EdenFeatures.pressureDisplayScale * 100f
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Edit Pressure HUD", desc = "Position the pressure display") @ConfigEditorButton(buttonText = "Open") @Transient @JvmField val editPressure = Runnable { Minecraft.getInstance().gui.setScreen(EdenHudEditorScreen(null, EdenHudWidget.PRESSURE)) }
    @ConfigAccordionId(id = 18)
    @ConfigOption(name = "Pressure Theme", desc = "Display theme") @ConfigEditorDropdown(values = ["Nighttime", "Peach"]) @Expose @JvmField var pressureTheme = EdenFeatures.pressureDisplayTheme
    @ConfigEditorAccordion(id = 19)
    @ConfigOption(name = "Low HP Indicator", desc = "Low HP indicator settings")
    @Transient @JvmField
    var lowHpGroup = false

    @ConfigAccordionId(id = 19)
    @ConfigOption(name = "Low HP Indicator", desc = "Screen warning at low health") @ConfigEditorBoolean @Expose @JvmField var lowHp = EdenFeatures.lowHpIndicatorEnabled
    @ConfigAccordionId(id = 19)
    @ConfigOption(name = "Low HP Transparency", desc = "Warning opacity percent") @ConfigEditorSlider(minValue = 20f, maxValue = 100f, minStep = 1f) @Expose @JvmField var lowHpAlpha = EdenFeatures.lowHpIndicatorTransparency * 100f
    @ConfigAccordionId(id = 19)
    @ConfigOption(name = "Low HP Pulse", desc = "Heartbeat animation") @ConfigEditorBoolean @Expose @JvmField var lowHpPulse = EdenFeatures.lowHpIndicatorHeartbeat
    @ConfigEditorAccordion(id = 20)
    @ConfigOption(name = "Drill Fuel Meter", desc = "Drill fuel HUD settings")
    @Transient @JvmField
    var drillGroup = false

    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Drill Fuel Meter", desc = "Show drill fuel") @ConfigEditorBoolean @Expose @JvmField var drill = EdenFeatures.drillFuelMeterEnabled
    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Drill Scale", desc = "HUD scale percent") @ConfigEditorSlider(minValue = 50f, maxValue = 200f, minStep = 5f) @Expose @JvmField var drillScale = EdenFeatures.drillFuelMeterScale * 100f
    @ConfigAccordionId(id = 20)
    @ConfigOption(name = "Edit Drill HUD", desc = "Position the drill meter") @ConfigEditorButton(buttonText = "Open") @Transient @JvmField val editDrill = Runnable { Minecraft.getInstance().gui.setScreen(EdenHudEditorScreen(null, EdenHudWidget.DRILL_FUEL)) }
    @ConfigOption(name = "Drill Fuel Prediction", desc = "Show approximately how many blocks remain") @ConfigEditorBoolean @Expose @JvmField var drillPrediction = EdenFeatures.drillFuelPredictionEnabled
    @ConfigEditorAccordion(id = 21)
    @ConfigOption(name = "Action Bar Cleanup", desc = "Action bar cleanup settings")
    @Transient @JvmField
    var cleanupGroup = false

    @ConfigAccordionId(id = 21)
    @ConfigOption(name = "Action Bar Cleanup", desc = "Remove duplicated HUD data") @ConfigEditorBoolean @Expose @JvmField var cleanup = EdenFeatures.actionBarCleanupEnabled
    @ConfigAccordionId(id = 21)
    @ConfigOption(name = "Hide Pressure in Action Bar", desc = "Remove pressure text") @ConfigEditorBoolean @Expose @JvmField var cleanupPressure = EdenFeatures.hidePressureInActionBar
    @ConfigAccordionId(id = 21)
    @ConfigOption(name = "Hide Drill Fuel in Action Bar", desc = "Remove fuel text") @ConfigEditorBoolean @Expose @JvmField var cleanupFuel = EdenFeatures.hideDrillFuelInActionBar

    fun save() {
        bool("Visuals.Player Hider", hider, true); bool("Visuals.Player Hider.Hide Players", hidePlayers); slider("Visuals.Player Hider.Distance", hideDistance, .5f, 10f); bool("Visuals.Player Hider.Hide All", hideAll); bool("Visuals.Player Hider.Ghost Mode", ghost); slider("Visuals.Player Hider.Opacity", ghostOpacity, 0f, 100f, "%"); bool("Visuals.Player Hider.Click Through Players", clickThrough)
        bool("Visuals.Player Size", size, true); bool("Visuals.Player Size.Scale All Players", sizeAll); slider("Visuals.Player Size.X Scale", sizeX, .1f, 3f); slider("Visuals.Player Size.Y Scale", sizeY, -3f, 3f); slider("Visuals.Player Size.Z Scale", sizeZ, .1f, 3f)
        bool("Visuals.Hit Color", hitColorEnabled, true); color("Visuals.Hit Color.Color", hitColor); bool("Visuals.Fullbright", fullbright, true)
        bool("Visuals.Performance HUD", performance, true); bool("Visuals.Performance HUD.Show FPS", showFps); bool("Visuals.Performance HUD.Show TPS", showTps); bool("Visuals.Performance HUD.Show Ping", showPing); bool("Visuals.Performance HUD.FPS Graph", showFpsGraph); bool("Visuals.Performance HUD.TPS Graph", showTpsGraph); bool("Visuals.Performance HUD.Ping Graph", showPingGraph); slider("Visuals.Performance HUD.Scale", performanceScale, 50f, 200f, "%")
        bool("Visuals.Render Optimizer", optimizer, true); bool("Visuals.Render Optimizer.Hide Falling Blocks", falling); bool("Visuals.Render Optimizer.Hide Lightning", lightning); bool("Visuals.Render Optimizer.Hide Experience Orbs", xpOrbs); bool("Visuals.Render Optimizer.Hide Death Animation", death); bool("Visuals.Render Optimizer.Hide Dying Armor Stands", armorStands); bool("Visuals.Render Optimizer.Hide Explosion Particles", explosions); bool("Visuals.Render Optimizer.Hide Archer Passive", archerPassive); bool("Visuals.Render Optimizer.Hide Healer Fairy", healerFairy); bool("Visuals.Render Optimizer.Hide Soul Weaver", soulWeaver); bool("Visuals.Render Optimizer.Hide Tentacle Head", tentacle); bool("Visuals.Render Optimizer.Hide Fire Overlay", fire)
        bool("Visuals.Name Replace", nameReplace, true); text("Visuals.Name Replace.Replacement", name); color("Visuals.Name Replace.Color", nameColor); bool("Visuals.Zoom", zoom, true); slider("Visuals.Zoom.Intensity", zoomIntensity.toFloat(), 1f, 10f); bool("Visuals.Zoom.Scrollable", zoomScrollable); writeKey("Visuals.Zoom.Keybind", zoomKey)
        bool("Visuals.Pet Overlay", pet, true); slider("Visuals.Pet Overlay.Scale", petScale, 50f, 200f, "%")
        bool("Visuals.Pressure Display", pressure, true); slider("Visuals.Pressure Display.Show At", pressureAt, 1f, 99f, "%"); slider("Visuals.Pressure Display.Scale", pressureScale, 50f, 200f, "%"); dropdown("Visuals.Pressure Display.Theme", pressureTheme)
        bool("Visuals.Low HP Indicator", lowHp, true); slider("Visuals.Low HP Indicator.Transparency", lowHpAlpha, 20f, 100f, "%"); bool("Visuals.Low HP Indicator.Pulse Animation", lowHpPulse)
        bool("Visuals.Drill Fuel Meter", drill, true); slider("Visuals.Drill Fuel Meter.Scale", drillScale, 50f, 200f, "%"); bool("Visuals.Drill Fuel Meter.Block Prediction", drillPrediction); bool("Visuals.Action Bar Cleanup", cleanup, true); bool("Visuals.Action Bar Cleanup.Hide Pressure", cleanupPressure); bool("Visuals.Action Bar Cleanup.Hide Drill Fuel", cleanupFuel)
    }
}

class MiscCategory {
	@ConfigEditorAccordion(id = 32)
	@ConfigOption(name = "Chat Shortcuts", desc = "Bind chat messages and commands to keys")
	@Transient @JvmField var chatShortcutsGroup = false
	@ConfigAccordionId(id = 32)
	@ConfigOption(name = "Chat Shortcuts", desc = "Put a / if you want it to run as command") @ConfigEditorChatShortcuts
	@Transient @JvmField var chatShortcutsEditor = false

    @ConfigEditorAccordion(id = 31)
    @ConfigOption(name = "Diana QoL", desc = "Click through foliage while using Diana tools")
    @Transient @JvmField var dianaGroup = false
    @ConfigAccordionId(id = 31)
    @ConfigOption(name = "Diana QoL", desc = "Target through selected foliage") @ConfigEditorBoolean @Expose @JvmField var diana = EdenFeatures.dianaQolEnabled
    @ConfigAccordionId(id = 31)
    @ConfigOption(name = "Ignore Grass and Ferns", desc = "Click through grass and fern blocks") @ConfigEditorBoolean @Expose @JvmField var dianaGrass = EdenFeatures.dianaIgnoreGrass
    @ConfigAccordionId(id = 31)
    @ConfigOption(name = "Ignore Flowers", desc = "Click through small flowers") @ConfigEditorBoolean @Expose @JvmField var dianaFlowers = EdenFeatures.dianaIgnoreFlowers
    @ConfigAccordionId(id = 31)
    @ConfigOption(name = "Ignore Bushes", desc = "Click through bushes and dead bushes") @ConfigEditorBoolean @Expose @JvmField var dianaBushes = EdenFeatures.dianaIgnoreBushes
    @ConfigOption(name = "Golden Fish CI", desc = "Golden Fish cast indicator") @ConfigEditorBoolean @Expose @JvmField var fish = EdenFeatures.goldenFishCiEnabled
    @ConfigOption(name = "Infinite Chat", desc = "Keep more chat history") @ConfigEditorBoolean @Expose @JvmField var chat = EdenFeatures.infiniteChatEnabled
    @ConfigOption(name = "Missing Enchants", desc = "Highlight missing enchantments") @ConfigEditorBoolean @Expose @JvmField var enchants = EdenFeatures.missingEnchantsEnabled
    @ConfigOption(name = "Compact Pet Level", desc = "Compact pet level text") @ConfigEditorBoolean @Expose @JvmField var petLevel = EdenFeatures.compactPetLevelEnabled

    fun save() {
        bool("Misc.Diana QoL", diana, true); bool("Misc.Diana QoL.Ignore Grass", dianaGrass); bool("Misc.Diana QoL.Ignore Flowers", dianaFlowers); bool("Misc.Diana QoL.Ignore Bushes", dianaBushes); bool("Misc.Golden Fish CI", fish, true); bool("Misc.Infinite Chat", chat, true); bool("Misc.Missing Enchants", enchants, true); bool("Misc.Compact Pet Level", petLevel, true)
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
private fun openEdenLink(url: String) {
    val client = Minecraft.getInstance()
    val parent = client.gui.screen() ?: return
    ConfirmLinkScreen.confirmLinkNow(parent, URI.create(url))
}
private fun readKey(path: String, fallback: Int = -1): Int {
    val name = EdenConfig.entry(path)?.keyName ?: return fallback
    return runCatching {
        val key = InputConstants.getKey(name)
        when (key.type) {
            InputConstants.Type.KEYBOARD -> MoulConfigInputCompat.toGlfwKey(key.value)
            InputConstants.Type.MOUSE -> MoulConfigInputCompat.toGlfwMouseButton(key.value)
        }.takeIf { it >= 0 } ?: fallback
    }.getOrDefault(fallback)
}
private fun writeKey(path: String, value: Int) = EdenConfig.updateEntry(path) {
    if (value == -1) { it.keyName = null; it.value = "n/a" }
    else {
        val key = if (value in 0..<32) {
            InputConstants.Type.MOUSE.getOrCreate(MoulConfigInputCompat.toSdlMouseButton(value))
        } else {
            val scancode = MoulConfigInputCompat.toSdlScancode(value)
            if (scancode < 0) null else InputConstants.Type.KEYBOARD.getOrCreate(scancode)
        }
        if (key == null) {
            it.keyName = null
            it.value = "n/a"
        } else {
            it.keyName = key.name
            it.value = key.displayName.string
        }
    }
}
