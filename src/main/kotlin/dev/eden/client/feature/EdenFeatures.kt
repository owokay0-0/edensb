package dev.eden.client.feature

import dev.eden.client.EdenConfig
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object EdenFeatures {
	var dungeonBreakerEnabled: Boolean = false
	var dungeonPreventMiningSecrets: Boolean = true
	var dungeonInstaMineWhenFatigue: Boolean = true
	var teammateHighlightEnabled: Boolean = false
	var shitterAlertEnabled: Boolean = true
	var shitterAlertScale: Float = 0.74f
	var shitterAlertX: Int = 0
	var shitterAlertY: Int = 420
	var etherwarpEnabled: Boolean = false
	var etherwarpShowGuess: Boolean = true
	var etherwarpShowFailed: Boolean = true
	var etherwarpRenderStyle: Int = 1
	var etherwarpUseServerPosition: Boolean = false
	var etherwarpFullBlock: Boolean = false
	var etherwarpDepth: Boolean = false
	var etherwarpLeftClickMode: Int = 2

	var playerHiderEnabled: Boolean = false
	var playerHiderHidePlayers: Boolean = false
	var playerHiderHideAll: Boolean = false
	var playerHiderGhostMode: Boolean = false
	var playerHiderClickThrough: Boolean = false
	var playerHiderDistance: Float = 1.5f
	var playerHiderGhostOpacity: Float = 0.15f
	var playerSizeEnabled: Boolean = false
	var playerSizeScaleAllPlayers: Boolean = true
	var playerSizeX: Float = 1.0f
	var playerSizeY: Float = 1.0f
	var playerSizeZ: Float = 1.0f
	var dianaQolEnabled: Boolean = false
	var dianaIgnoreGrass: Boolean = true
	var dianaIgnoreFlowers: Boolean = true
	var dianaIgnoreBushes: Boolean = true
	var goldenFishCiEnabled: Boolean = false
	var hitColorEnabled: Boolean = true
	var infiniteChatEnabled: Boolean = false
	var fullbrightEnabled: Boolean = false
	var zoomEnabled: Boolean = true
	var zoomIntensity: Int = 5
	var zoomScrollable: Boolean = true
	var performanceHudEnabled: Boolean = false
	var performanceHudShowFps: Boolean = true
	var performanceHudShowTps: Boolean = true
	var performanceHudShowPing: Boolean = true
	var performanceHudShowFpsGraph: Boolean = true
	var performanceHudShowTpsGraph: Boolean = true
	var performanceHudShowPingGraph: Boolean = true
	var performanceHudScale: Float = 1.0f
	var performanceHudX: Int = 8
	var performanceHudY: Int = 8
	var renderOptimizerEnabled: Boolean = false
	var renderOptimizerHideFallingBlocks: Boolean = true
	var renderOptimizerHideLightning: Boolean = true
	var renderOptimizerHideExperienceOrbs: Boolean = true
	var renderOptimizerHideDeathAnimation: Boolean = true
	var renderOptimizerHideDyingArmorStands: Boolean = false
	var renderOptimizerHideExplosionParticles: Boolean = false
	var renderOptimizerHideArcherPassive: Boolean = true
	var renderOptimizerHideHealerFairy: Boolean = true
	var renderOptimizerHideSoulWeaver: Boolean = true
	var renderOptimizerHideTentacleHead: Boolean = true
	var renderOptimizerHideFireOverlay: Boolean = true
	var nameReplaceEnabled: Boolean = false
	var nameReplacement: String = "Eden"

	var petOverlayEnabled: Boolean = true
	var petOverlayScale: Float = 1.0f
	var petOverlayX: Int = 65
	var petOverlayY: Int = -40

	var pressureDisplayEnabled: Boolean = true
	var pressureDisplayShowAt: Float = 0.05f
	var pressureDisplayScale: Float = 1.0f
	var pressureDisplayTheme: Int = 0
	var pressureDisplayX: Int = -90
	var pressureDisplayY: Int = -55

	var drillFuelMeterEnabled: Boolean = true
	var drillFuelMeterScale: Float = 1.0f
	var drillFuelPredictionEnabled: Boolean = true
	var drillFuelMeterX: Int = -120
	var drillFuelMeterY: Int = -65

	var dungeonScoreMeterEnabled: Boolean = true
	var dungeonScoreMeterScale: Float = 1.0f
	var dungeonScoreMeterX: Int = -336
	var dungeonScoreMeterY: Int = -142

	var lowHpIndicatorEnabled: Boolean = true
	var lowHpIndicatorHeartbeat: Boolean = true
	var lowHpIndicatorTransparency: Float = 0.4f

	var missingEnchantsEnabled: Boolean = true
	var compactPetLevelEnabled: Boolean = true
	var actionBarCleanupEnabled: Boolean = true
	var hidePressureInActionBar: Boolean = false
	var hideDrillFuelInActionBar: Boolean = false

	val etherwarpColor = ColorState(0x80FFAA00.toInt())
	val etherwarpFailColor = ColorState(0x80FF5555.toInt())
	val archerColor = ColorState(0xFFFFBC0A.toInt())
	val berserkerColor = ColorState(0xFF880015.toInt())
	val tankColor = ColorState(0xFF188037.toInt())
	val mageColor = ColorState(0xFF00A2E8.toInt())
	val healerColor = ColorState(0xFFFFAFCA.toInt())
	val hitColor = ColorState(0xB2FF0000.toInt())
	val nameReplaceColor = ColorState(0xFFAAA4FF.toInt())
	val dungeonScoreColor = ColorState(0xFFFFFFFF.toInt())

	val etherwarpRenderStyles = arrayOf("Filled", "Outline", "Filled Outline")
	val etherwarpLeftClickModes = arrayOf("Off", "Left Click", "Left Click + Shift")
	val pressureThemes = arrayOf("Nighttime", "Peach")

	private val roleColors = mapOf(
		'A' to archerColor,
		'B' to berserkerColor,
		'T' to tankColor,
		'M' to mageColor,
		'H' to healerColor,
	)

	fun colorForRole(role: Char): ColorState? = roleColors[role.uppercaseChar()]

	fun loadFromConfig() {
		EdenConfig.entry("Dungeons.DungeonBreaker")?.enabled?.let { dungeonBreakerEnabled = it }
		EdenConfig.entry("Dungeons.DungeonBreaker.Prevent mining secrets")?.switchValue?.let { dungeonPreventMiningSecrets = it }
		EdenConfig.entry("Dungeons.DungeonBreaker.Insta-mine when fatigue")?.switchValue?.let { dungeonInstaMineWhenFatigue = it }
		EdenConfig.entry("Dungeons.Teammate Highlight")?.enabled?.let { teammateHighlightEnabled = it }
		EdenConfig.entry("Dungeons.Shitter Alert")?.enabled?.let { shitterAlertEnabled = it }
		shitterAlertScale = slider("Dungeons.Shitter Alert.Scale", 0.5f, 2.0f, shitterAlertScale)
		shitterAlertX = intValue("Dungeons.Shitter Alert.Hud X", shitterAlertX)
		shitterAlertY = intValue("Dungeons.Shitter Alert.Hud Y", shitterAlertY)
		EdenConfig.entry("Dungeons.Etherwarp")?.enabled?.let { etherwarpEnabled = it }
		EdenConfig.entry("Dungeons.Etherwarp.Show Guess")?.switchValue?.let { etherwarpShowGuess = it }
		EdenConfig.entry("Dungeons.Etherwarp.Show when failed")?.switchValue?.let { etherwarpShowFailed = it }
		EdenConfig.entry("Dungeons.Etherwarp.Render Style")?.selected?.let {
			etherwarpRenderStyle = it.coerceIn(0, etherwarpRenderStyles.lastIndex)
		}
		EdenConfig.entry("Dungeons.Etherwarp.Use Server Position")?.switchValue?.let { etherwarpUseServerPosition = it }
		EdenConfig.entry("Dungeons.Etherwarp.Full Block")?.switchValue?.let { etherwarpFullBlock = it }
		EdenConfig.entry("Dungeons.Etherwarp.Depth")?.switchValue?.let { etherwarpDepth = it }
		EdenConfig.entry("Dungeons.Etherwarp.Left Click Mode")?.selected?.let {
			etherwarpLeftClickMode = it.coerceIn(0, etherwarpLeftClickModes.lastIndex)
		}
		EdenConfig.entry("Dungeons.Etherwarp.Color")?.color?.let { etherwarpColor.setArgb(it) }
		EdenConfig.entry("Dungeons.Etherwarp.Fail Color")?.color?.let { etherwarpFailColor.setArgb(it) }
		EdenConfig.entry("Dungeons.Teammate Highlight.Archer Color")?.color?.let { archerColor.setArgb(it) }
		EdenConfig.entry("Dungeons.Teammate Highlight.Berserker Color")?.color?.let { berserkerColor.setArgb(it) }
		EdenConfig.entry("Dungeons.Teammate Highlight.Tank Color")?.color?.let { tankColor.setArgb(it) }
		EdenConfig.entry("Dungeons.Teammate Highlight.Mage Color")?.color?.let { mageColor.setArgb(it) }
		EdenConfig.entry("Dungeons.Teammate Highlight.Healer Color")?.color?.let { healerColor.setArgb(it) }

		EdenConfig.entry("Visuals.Player Hider")?.enabled?.let { playerHiderEnabled = it }
		playerHiderHidePlayers = switch("Visuals.Player Hider.Hide Players", playerHiderHidePlayers)
		playerHiderHideAll = switch("Visuals.Player Hider.Hide All", playerHiderHideAll)
		playerHiderGhostMode = switch("Visuals.Player Hider.Ghost Mode", playerHiderGhostMode)
		playerHiderClickThrough = switch("Visuals.Player Hider.Click Through Players", playerHiderClickThrough)
		playerHiderDistance = slider("Visuals.Player Hider.Distance", 0.5f, 10.0f, playerHiderDistance)
		playerHiderGhostOpacity = slider("Visuals.Player Hider.Opacity", 0.0f, 1.0f, playerHiderGhostOpacity)

		EdenConfig.entry("Visuals.Player Size")?.enabled?.let { playerSizeEnabled = it }
		playerSizeScaleAllPlayers = switch("Visuals.Player Size.Scale All Players", playerSizeScaleAllPlayers)
		playerSizeX = slider("Visuals.Player Size.X Scale", 0.1f, 3.0f, playerSizeX)
		playerSizeY = slider("Visuals.Player Size.Y Scale", -3.0f, 3.0f, playerSizeY)
		playerSizeZ = slider("Visuals.Player Size.Z Scale", 0.1f, 3.0f, playerSizeZ)

		EdenConfig.entry("Misc.Diana QoL")?.enabled?.let { dianaQolEnabled = it }
		dianaIgnoreGrass = switch("Misc.Diana QoL.Ignore Grass", dianaIgnoreGrass)
		dianaIgnoreFlowers = switch("Misc.Diana QoL.Ignore Flowers", dianaIgnoreFlowers)
		dianaIgnoreBushes = switch("Misc.Diana QoL.Ignore Bushes", dianaIgnoreBushes)
		EdenConfig.entry("Misc.Golden Fish CI")?.enabled?.let { goldenFishCiEnabled = it }
		EdenConfig.entry("Visuals.Hit Color")?.enabled?.let { hitColorEnabled = it }
		color("Visuals.Hit Color.Color", hitColor)
		EdenConfig.entry("Misc.Infinite Chat")?.enabled?.let { infiniteChatEnabled = it }
		EdenConfig.entry("Visuals.Fullbright")?.enabled?.let { fullbrightEnabled = it }
		EdenConfig.entry("Visuals.Zoom")?.enabled?.let { zoomEnabled = it }
		zoomIntensity = slider("Visuals.Zoom.Intensity", 1.0f, 10.0f, zoomIntensity.toFloat()).roundToInt().coerceIn(1, 10)
		zoomScrollable = switch("Visuals.Zoom.Scrollable", zoomScrollable)
		EdenConfig.entry("Visuals.Performance HUD")?.enabled?.let { performanceHudEnabled = it }
		performanceHudShowFps = switch("Visuals.Performance HUD.Show FPS", performanceHudShowFps)
		performanceHudShowTps = switch("Visuals.Performance HUD.Show TPS", performanceHudShowTps)
		performanceHudShowPing = switch("Visuals.Performance HUD.Show Ping", performanceHudShowPing)
		val legacyGraphs = EdenConfig.entry("Visuals.Performance HUD.Show Graphs")?.switchValue ?: true
		performanceHudShowFpsGraph = switch("Visuals.Performance HUD.FPS Graph", legacyGraphs)
		performanceHudShowTpsGraph = switch("Visuals.Performance HUD.TPS Graph", legacyGraphs)
		performanceHudShowPingGraph = switch("Visuals.Performance HUD.Ping Graph", legacyGraphs)
		performanceHudScale = slider("Visuals.Performance HUD.Scale", 0.5f, 2.0f, performanceHudScale)
		performanceHudX = intValue("Visuals.Performance HUD.Hud X", performanceHudX)
		performanceHudY = intValue("Visuals.Performance HUD.Hud Y", performanceHudY)

		EdenConfig.entry("Visuals.Render Optimizer")?.enabled?.let { renderOptimizerEnabled = it }
		renderOptimizerHideFallingBlocks = switch(
			"Visuals.Render Optimizer.Hide Falling Blocks",
			renderOptimizerHideFallingBlocks,
		)
		renderOptimizerHideLightning = switch("Visuals.Render Optimizer.Hide Lightning", renderOptimizerHideLightning)
		renderOptimizerHideExperienceOrbs = switch(
			"Visuals.Render Optimizer.Hide Experience Orbs",
			renderOptimizerHideExperienceOrbs,
		)
		renderOptimizerHideDeathAnimation = switch(
			"Visuals.Render Optimizer.Hide Death Animation",
			renderOptimizerHideDeathAnimation,
		)
		renderOptimizerHideDyingArmorStands = switch(
			"Visuals.Render Optimizer.Hide Dying Armor Stands",
			renderOptimizerHideDyingArmorStands,
		)
		renderOptimizerHideExplosionParticles = switch(
			"Visuals.Render Optimizer.Hide Explosion Particles",
			renderOptimizerHideExplosionParticles,
		)
		renderOptimizerHideArcherPassive = switch(
			"Visuals.Render Optimizer.Hide Archer Passive",
			renderOptimizerHideArcherPassive,
		)
		renderOptimizerHideHealerFairy = switch(
			"Visuals.Render Optimizer.Hide Healer Fairy",
			renderOptimizerHideHealerFairy,
		)
		renderOptimizerHideSoulWeaver = switch(
			"Visuals.Render Optimizer.Hide Soul Weaver",
			renderOptimizerHideSoulWeaver,
		)
		renderOptimizerHideTentacleHead = switch(
			"Visuals.Render Optimizer.Hide Tentacle Head",
			renderOptimizerHideTentacleHead,
		)
		renderOptimizerHideFireOverlay = switch(
			"Visuals.Render Optimizer.Hide Fire Overlay",
			renderOptimizerHideFireOverlay,
		)

		EdenConfig.entry("Visuals.Name Replace")?.enabled?.let { nameReplaceEnabled = it }
		nameReplacement = stringValue("Visuals.Name Replace.Replacement", nameReplacement)
		color("Visuals.Name Replace.Color", nameReplaceColor)

		HitColorFeature.refresh()
		NameReplaceFeature.refresh()

		EdenConfig.entry("Visuals.Pet Overlay")?.enabled?.let { petOverlayEnabled = it }
		petOverlayScale = slider("Visuals.Pet Overlay.Scale", 0.5f, 2.0f, petOverlayScale)
		petOverlayX = intValue("Visuals.Pet Overlay.Hud X", petOverlayX)
		petOverlayY = intValue("Visuals.Pet Overlay.Hud Y", petOverlayY)

		EdenConfig.entry("Visuals.Pressure Display")?.enabled?.let { pressureDisplayEnabled = it }
		pressureDisplayShowAt = slider("Visuals.Pressure Display.Show At", 0.01f, 0.99f, pressureDisplayShowAt)
		pressureDisplayScale = slider("Visuals.Pressure Display.Scale", 0.5f, 2.0f, pressureDisplayScale)
		pressureDisplayTheme = selected("Visuals.Pressure Display.Theme", pressureDisplayTheme, pressureThemes.lastIndex)
		pressureDisplayX = intValue("Visuals.Pressure Display.Hud X", pressureDisplayX)
		pressureDisplayY = intValue("Visuals.Pressure Display.Hud Y", pressureDisplayY)

		EdenConfig.entry("Visuals.Drill Fuel Meter")?.enabled?.let { drillFuelMeterEnabled = it }
		drillFuelMeterScale = slider("Visuals.Drill Fuel Meter.Scale", 0.5f, 2.0f, drillFuelMeterScale)
		drillFuelPredictionEnabled = switch("Visuals.Drill Fuel Meter.Block Prediction", drillFuelPredictionEnabled)
		drillFuelMeterX = intValue("Visuals.Drill Fuel Meter.Hud X", drillFuelMeterX)
		drillFuelMeterY = intValue("Visuals.Drill Fuel Meter.Hud Y", drillFuelMeterY)

		EdenConfig.entry("Dungeons.Dungeon Score Meter")?.enabled?.let { dungeonScoreMeterEnabled = it }
		dungeonScoreMeterScale = slider("Dungeons.Dungeon Score Meter.Scale", 0.5f, 2.0f, dungeonScoreMeterScale)
		color("Dungeons.Dungeon Score Meter.Color", dungeonScoreColor)
		dungeonScoreMeterX = intValue("Dungeons.Dungeon Score Meter.Hud X", dungeonScoreMeterX)
		dungeonScoreMeterY = intValue("Dungeons.Dungeon Score Meter.Hud Y", dungeonScoreMeterY)

		EdenConfig.entry("Visuals.Low HP Indicator")?.enabled?.let { lowHpIndicatorEnabled = it }
		lowHpIndicatorTransparency = slider(
			"Visuals.Low HP Indicator.Transparency",
			0.2f,
			1.0f,
			lowHpIndicatorTransparency,
		)
		lowHpIndicatorHeartbeat = switch("Visuals.Low HP Indicator.Pulse Animation", lowHpIndicatorHeartbeat)

		EdenConfig.entry("Misc.Missing Enchants")?.enabled?.let { missingEnchantsEnabled = it }
		EdenConfig.entry("Misc.Compact Pet Level")?.enabled?.let { compactPetLevelEnabled = it }
		EdenConfig.entry("Visuals.Action Bar Cleanup")?.enabled?.let { actionBarCleanupEnabled = it }
		hidePressureInActionBar = switch("Visuals.Action Bar Cleanup.Hide Pressure", hidePressureInActionBar)
		hideDrillFuelInActionBar = switch("Visuals.Action Bar Cleanup.Hide Drill Fuel", hideDrillFuelInActionBar)
	}

	fun saveHudLayout(prefix: String, x: Int, y: Int, scale: Float) {
		EdenConfig.updateEntry("$prefix.Hud X") { it.value = x.toString() }
		EdenConfig.updateEntry("$prefix.Hud Y") { it.value = y.toString() }
		val range = 2.0f - 0.5f
		EdenConfig.updateEntry("$prefix.Scale") {
			it.value = "${(scale * 100.0f).toInt()}%"
			it.sliderPercentage = ((scale - 0.5f) / range).coerceIn(0.0f, 1.0f)
		}
		loadFromConfig()
	}

	private fun selected(key: String, default: Int, lastIndex: Int): Int {
		return EdenConfig.entry(key)?.selected?.coerceIn(0, lastIndex) ?: default
	}

	private fun switch(key: String, default: Boolean): Boolean {
		return EdenConfig.entry(key)?.switchValue ?: default
	}

	private fun slider(key: String, min: Float, max: Float, default: Float): Float {
		val percentage = EdenConfig.entry(key)?.sliderPercentage ?: return default
		return min + (max - min) * percentage.coerceIn(0.0f, 1.0f)
	}

	private fun intValue(key: String, default: Int): Int {
		return EdenConfig.entry(key)?.value?.toIntOrNull() ?: default
	}

	private fun stringValue(key: String, default: String): String {
		return EdenConfig.entry(key)?.value ?: default
	}

	private fun color(key: String, state: ColorState) {
		EdenConfig.entry(key)?.color?.let(state::setArgb)
	}

	class ColorState(defaultArgb: Int) {
		var hue: Float = 0.0f
			private set
		var saturation: Float = 0.0f
			private set
		var brightness: Float = 0.0f
			private set
		var alpha: Float = 1.0f
			private set

		val argb: Int
			get() {
				val rgb = hsbToRgb(hue, saturation, brightness)
				val a = (alpha.coerceIn(0.0f, 1.0f) * 255.0f).toInt().coerceIn(0, 255)
				return (a shl 24) or (rgb and 0x00FFFFFF)
			}

		init {
			setArgb(defaultArgb)
		}

		fun setArgb(value: Int) {
			val r = (value ushr 16) and 0xFF
			val g = (value ushr 8) and 0xFF
			val b = value and 0xFF
			val hsb = rgbToHsb(r, g, b)
			hue = hsb[0]
			saturation = hsb[1]
			brightness = hsb[2]
			alpha = ((value ushr 24) and 0xFF) / 255.0f
		}

		fun setSaturationBrightness(newSaturation: Float, newBrightness: Float) {
			saturation = newSaturation.coerceIn(0.0f, 1.0f)
			brightness = newBrightness.coerceIn(0.0f, 1.0f)
		}

		fun setHue(newHue: Float) {
			hue = newHue.coerceIn(0.0f, 1.0f)
		}

		fun setAlpha(newAlpha: Float) {
			alpha = newAlpha.coerceIn(0.0f, 1.0f)
		}

		fun hueArgb(): Int {
			return (0xFF shl 24) or (hsbToRgb(hue, 1.0f, 1.0f) and 0x00FFFFFF)
		}

		fun hex(includeAlpha: Boolean = true): String {
			val value = argb
			val r = (value ushr 16) and 0xFF
			val g = (value ushr 8) and 0xFF
			val b = value and 0xFF
			val a = (value ushr 24) and 0xFF
			return if (includeAlpha) {
				String.format(Locale.ROOT, "%02X%02X%02X%02X", r, g, b, a)
			} else {
				String.format(Locale.ROOT, "%02X%02X%02X", r, g, b)
			}
		}
	}

	private fun rgbToHsb(r: Int, g: Int, b: Int): FloatArray {
		val red = r / 255.0f
		val green = g / 255.0f
		val blue = b / 255.0f
		val max = max(red, max(green, blue))
		val min = min(red, min(green, blue))
		val delta = max - min
		val hue = when {
			delta == 0.0f -> 0.0f
			max == red -> ((green - blue) / delta) % 6.0f
			max == green -> (blue - red) / delta + 2.0f
			else -> (red - green) / delta + 4.0f
		}
		val normalizedHue = (hue / 6.0f).let { if (it < 0.0f) it + 1.0f else it }
		val saturation = if (max == 0.0f) 0.0f else delta / max
		return floatArrayOf(normalizedHue, saturation, max)
	}

	fun hsbToRgb(hue: Float, saturation: Float, brightness: Float): Int {
		val h = (hue.coerceIn(0.0f, 1.0f) * 6.0f).let { if (it == 6.0f) 0.0f else it }
		val s = saturation.coerceIn(0.0f, 1.0f)
		val v = brightness.coerceIn(0.0f, 1.0f)
		val sector = h.toInt()
		val fraction = h - sector
		val p = v * (1.0f - s)
		val q = v * (1.0f - s * fraction)
		val t = v * (1.0f - s * (1.0f - fraction))
		val (r, g, b) = when (sector) {
			0 -> Triple(v, t, p)
			1 -> Triple(q, v, p)
			2 -> Triple(p, v, t)
			3 -> Triple(p, q, v)
			4 -> Triple(t, p, v)
			else -> Triple(v, p, q)
		}
		return ((r * 255.0f).toInt().coerceIn(0, 255) shl 16) or
			((g * 255.0f).toInt().coerceIn(0, 255) shl 8) or
			(b * 255.0f).toInt().coerceIn(0, 255)
	}
}
