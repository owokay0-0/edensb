package dev.eden.client.feature

import dev.eden.client.EdenClient
import dev.eden.client.EdenHudWidget
import dev.eden.client.render.EdenNvgPipRenderer
import dev.eden.client.render.EdenNvgRenderer
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object SkyblockHudRenderer {
	data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)

	private var animatedPressure = 0.0f
	private var animatedFuel = 0.0f
	private var animatedScore = 0.0f

	fun register() {
		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CROSSHAIR,
			Identifier.fromNamespaceAndPath(EdenClient.MOD_ID, "low_hp_indicator"),
		::extractLowHealth,
		)
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.HOTBAR,
			Identifier.fromNamespaceAndPath(EdenClient.MOD_ID, "skyblock_widgets"),
			::extractWidgets,
		)
	}

	fun renderEditor(graphics: GuiGraphicsExtractor, widget: EdenHudWidget) {
		renderWidgets(graphics, widget, showAllPreviews = true)
		for (candidate in EdenHudWidget.entries) {
			val bounds = bounds(candidate, graphics.guiWidth(), graphics.guiHeight())
			graphics.outline(
				bounds.x.toInt() - 2,
				bounds.y.toInt() - 2,
				bounds.width.toInt() + 4,
				bounds.height.toInt() + 4,
				if (candidate == widget) 0xCCAAA4FF.toInt() else 0x55FFFFFF,
			)
		}
	}

	fun bounds(widget: EdenHudWidget, screenWidth: Int, screenHeight: Int): Bounds {
		val (width, height) = baseSize(widget)
		val scale = scale(widget)
		val scaledWidth = width * scale
		val scaledHeight = height * scale
		val (anchorX, anchorY) = anchoredPosition(
			anchor(widget),
			screenWidth.toFloat(),
			screenHeight.toFloat(),
			scaledWidth,
			scaledHeight,
		)
		return Bounds(
			anchorX + offsetX(widget),
			anchorY + offsetY(widget),
			scaledWidth,
			scaledHeight,
		)
	}

	fun updateLayout(widget: EdenHudWidget, x: Int, y: Int, scale: Float) {
		when (widget) {
			EdenHudWidget.PET -> {
				EdenFeatures.petOverlayX = x
				EdenFeatures.petOverlayY = y
				EdenFeatures.petOverlayScale = scale
			}

			EdenHudWidget.PRESSURE -> {
				EdenFeatures.pressureDisplayX = x
				EdenFeatures.pressureDisplayY = y
				EdenFeatures.pressureDisplayScale = scale
			}

			EdenHudWidget.DRILL_FUEL -> {
				EdenFeatures.drillFuelMeterX = x
				EdenFeatures.drillFuelMeterY = y
				EdenFeatures.drillFuelMeterScale = scale
			}

			EdenHudWidget.DUNGEON_SCORE -> {
				EdenFeatures.dungeonScoreMeterX = x
				EdenFeatures.dungeonScoreMeterY = y
				EdenFeatures.dungeonScoreMeterScale = scale
			}

			EdenHudWidget.SHITTER_ALERT -> {
				EdenFeatures.shitterAlertX = x
				EdenFeatures.shitterAlertY = y
				EdenFeatures.shitterAlertScale = scale
			}

			EdenHudWidget.PERFORMANCE -> {
				EdenFeatures.performanceHudX = x
				EdenFeatures.performanceHudY = y
				EdenFeatures.performanceHudScale = scale
			}
		}
	}

	fun currentOffset(widget: EdenHudWidget): Pair<Int, Int> = offsetX(widget).toInt() to offsetY(widget).toInt()

	fun currentScale(widget: EdenHudWidget): Float = scale(widget)

	fun resetLayout(widget: EdenHudWidget) {
		when (widget) {
			EdenHudWidget.PET -> updateLayout(widget, 65, -40, 1.0f)
			EdenHudWidget.PRESSURE -> updateLayout(widget, -90, -55, 1.0f)
			EdenHudWidget.DRILL_FUEL -> updateLayout(widget, -120, -65, 1.0f)
			EdenHudWidget.DUNGEON_SCORE -> updateLayout(widget, -336, -142, 1.0f)
			EdenHudWidget.SHITTER_ALERT -> updateLayout(widget, 0, 420, 0.74f)
			EdenHudWidget.PERFORMANCE -> updateLayout(widget, 8, 8, 1.0f)
		}
	}

	fun persistLayout(widget: EdenHudWidget) {
		val (x, y) = currentOffset(widget)
		EdenFeatures.saveHudLayout(widget.configPrefix, x, y, currentScale(widget))
	}

	private fun extractLowHealth(graphics: GuiGraphicsExtractor, ignored: net.minecraft.client.DeltaTracker) {
		if (!EdenFeatures.lowHpIndicatorEnabled || !SkyblockDataTracker.shouldRender()) {
			return
		}
		val health = SkyblockDataTracker.health.coerceIn(0.0f, 1.0f)
		if (health >= 0.5f) {
			return
		}

		val danger = ((0.5f - health) / 0.5f).coerceIn(0.0f, 1.0f)
		val pulse = if (EdenFeatures.lowHpIndicatorHeartbeat) {
			val progress = (System.currentTimeMillis() % 1000L) / 1000.0f
			1.0f - progress * progress
		} else {
			1.0f
		}
		val alpha = (170.0f * EdenFeatures.lowHpIndicatorTransparency * danger * (1.0f + pulse * 0.5f))
			.toInt()
			.coerceIn(0, 255)
		val strong = withAlpha(0xFF0000, alpha)
		val weak = withAlpha(0xFF0000, alpha / 4)
		val width = graphics.guiWidth().toFloat()
		val height = graphics.guiHeight().toFloat()
		val edge = (width.coerceAtMost(height) * 0.24f).coerceAtLeast(48.0f)

		EdenNvgPipRenderer.draw(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight()) {
			EdenNvgRenderer.gradientRect(0.0f, 0.0f, edge, height, 0.0f, strong, weak, false)
			EdenNvgRenderer.gradientRect(width - edge, 0.0f, edge, height, 0.0f, weak, strong, false)
			EdenNvgRenderer.gradientRect(0.0f, 0.0f, width, edge, 0.0f, strong, weak, true)
			EdenNvgRenderer.gradientRect(0.0f, height - edge, width, edge, 0.0f, weak, strong, true)
		}
	}

	private fun extractWidgets(graphics: GuiGraphicsExtractor, ignored: net.minecraft.client.DeltaTracker) {
		PerformanceMetricsFeature.recordFrame()
		renderWidgets(graphics, null)
	}

	private fun renderWidgets(
		graphics: GuiGraphicsExtractor,
		previewWidget: EdenHudWidget?,
		showAllPreviews: Boolean = false,
	) {
		val preview = previewWidget != null || showAllPreviews
		val renderSkyblockWidgets = preview || SkyblockDataTracker.shouldRender()
		val renderPerformance = showAllPreviews || previewWidget == EdenHudWidget.PERFORMANCE ||
			(!preview && EdenFeatures.performanceHudEnabled)
		if (!renderSkyblockWidgets && !renderPerformance) {
			return
		}

		val now = System.currentTimeMillis()
		val visible = ArrayList<EdenHudWidget>(6)
		if (
			showAllPreviews || (previewWidget == EdenHudWidget.PET) ||
			(!preview && renderSkyblockWidgets && EdenFeatures.petOverlayEnabled && SkyblockDataTracker.petActive)
		) {
			visible.add(EdenHudWidget.PET)
		}
		if (
			showAllPreviews || (previewWidget == EdenHudWidget.PRESSURE) ||
			(!preview && renderSkyblockWidgets && EdenFeatures.pressureDisplayEnabled && SkyblockDataTracker.inWater &&
				SkyblockDataTracker.pressure >= EdenFeatures.pressureDisplayShowAt)
		) {
			visible.add(EdenHudWidget.PRESSURE)
		}
		if (
			showAllPreviews || (previewWidget == EdenHudWidget.DRILL_FUEL) ||
			(!preview && renderSkyblockWidgets && EdenFeatures.drillFuelMeterEnabled &&
				now - SkyblockDataTracker.lastFuelSeenAt < 1400L)
		) {
			visible.add(EdenHudWidget.DRILL_FUEL)
		}
		if (
			showAllPreviews || (previewWidget == EdenHudWidget.DUNGEON_SCORE) ||
			(!preview && renderSkyblockWidgets && EdenFeatures.dungeonScoreMeterEnabled && SkyblockDataTracker.inDungeon)
		) {
			visible.add(EdenHudWidget.DUNGEON_SCORE)
		}
		if (
			showAllPreviews || (previewWidget == EdenHudWidget.SHITTER_ALERT) ||
			(!preview && EdenFeatures.shitterAlertEnabled && ShitterAlertFeature.visible())
		) {
			visible.add(EdenHudWidget.SHITTER_ALERT)
		}
		if (renderPerformance) {
			visible.add(EdenHudWidget.PERFORMANCE)
		}
		if (visible.isEmpty()) {
			return
		}

		updateAnimations(preview)
		EdenNvgPipRenderer.draw(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight()) {
			for (widget in visible) {
				val bounds = bounds(widget, graphics.guiWidth(), graphics.guiHeight())
				EdenNvgRenderer.push()
				EdenNvgRenderer.translate(bounds.x, bounds.y)
				EdenNvgRenderer.scale(scale(widget), scale(widget))
				when (widget) {
					EdenHudWidget.PET -> drawPet(preview)
					EdenHudWidget.PRESSURE -> drawPressure(preview)
					EdenHudWidget.DRILL_FUEL -> drawFuel(preview)
					EdenHudWidget.DUNGEON_SCORE -> drawDungeonScore(preview)
					EdenHudWidget.SHITTER_ALERT -> drawShitterAlert(preview)
					EdenHudWidget.PERFORMANCE -> drawPerformance(preview)
				}
				EdenNvgRenderer.pop()
			}
		}
	}

	private fun updateAnimations(preview: Boolean) {
		val pressure = if (preview) 0.64f else SkyblockDataTracker.pressure
		val fuel = if (preview) 0.72f else SkyblockDataTracker.fuelProgress()
		val score = if (preview) 286.0f else SkyblockDataTracker.dungeonScore
		animatedPressure = lerp(animatedPressure, pressure)
		animatedFuel = lerp(animatedFuel, fuel)
		animatedScore = lerp(animatedScore, score)
	}

	private fun drawPet(preview: Boolean) {
		val prefix = "Active pet: "
		val name = if (preview) "Golden Dragon" else SkyblockDataTracker.petName.ifBlank { "Unknown" }
		val level = if (preview) 200 else SkyblockDataTracker.petLevel
		val rarity = if (preview) "legendary" else SkyblockDataTracker.petRarity
		val textSize = 10.0f
		EdenNvgRenderer.text(prefix, 0.0f, 0.0f, textSize, 0xFFFFFFFF.toInt())
		EdenNvgRenderer.text(
			"$name LVL $level",
			EdenNvgRenderer.textWidth(prefix, textSize).roundToInt().toFloat(),
			0.0f,
			textSize,
			petRarityColor(rarity),
		)
	}

	private fun petRarityColor(rarity: String): Int = when (rarity.lowercase(java.util.Locale.ROOT)) {
		"special", "very_special" -> 0xFFFF5555.toInt()
		"divine" -> 0xFF55FFFF.toInt()
		"mythic" -> 0xFFFF55FF.toInt()
		"legendary" -> 0xFFFFAA00.toInt()
		"epic" -> 0xFFAA00AA.toInt()
		"rare" -> 0xFF5555FF.toInt()
		"uncommon" -> 0xFF55FF55.toInt()
		else -> 0xFFFFFFFF.toInt()
	}

	private fun drawPressure(preview: Boolean) {
		val colors = if (EdenFeatures.pressureDisplayTheme == 1) {
			Triple(0xFFFFDAB9.toInt(), 0xFFF0A080.toInt(), 0xFFAC5F4A.toInt())
		} else {
			Triple(0xFFAFAFAF.toInt(), 0xFF3D3D41.toInt(), 0xFF1D1D21.toInt())
		}
		val cx = 17.0f
		val cy = 27.0f
		EdenNvgRenderer.circle(cx, cy, 14.0f, colors.second)
		EdenNvgRenderer.circle(cx, cy, 12.5f, colors.third)
		val start = Math.toRadians(135.0).toFloat()
		val sweep = Math.toRadians(270.0).toFloat()
		for (i in 0..8) {
			val angle = start + sweep * i / 8.0f
			val x1 = cx + cos(angle) * 8.0f
			val y1 = cy + sin(angle) * 8.0f
			val x2 = cx + cos(angle) * 11.0f
			val y2 = cy + sin(angle) * 11.0f
			EdenNvgRenderer.line(x1, y1, x2, y2, if (i == 8) 1.2f else 0.7f, if (i == 8) 0xFF993333.toInt() else withAlpha(colors.second, 180))
		}
		val needle = start + sweep * animatedPressure
		EdenNvgRenderer.line(cx, cy, cx + cos(needle) * 10.0f, cy + sin(needle) * 10.0f, 1.4f, colors.first)
		EdenNvgRenderer.circle(cx, cy, 2.0f, colors.second)
		centerText("${((if (preview) 0.64f else SkyblockDataTracker.pressure) * 100).toInt()}%", cx, 1.0f, 9.0f, 0xFFFFFFFF.toInt())
	}

	private fun drawFuel(preview: Boolean) {
		val text = if (EdenFeatures.drillFuelPredictionEnabled) {
			val blocks = if (preview) 2_160 else SkyblockDataTracker.fuelCurrent.coerceAtLeast(0)
			"~${compactNumber(blocks)} BLOCKS"
		} else {
			val value = if (preview) 72.0f else animatedFuel * 100.0f
			(if (value >= 10.0f) "%.0f".format(value) else "%.1f".format(value)) + "% FUEL"
		}
		EdenNvgRenderer.text(text, 0.0f, 0.0f, 11.0f, 0xFFFFFFFF.toInt())
	}

	private fun drawDungeonScore(preview: Boolean) {
		val score = if (preview && animatedScore < 1.0f) 286.0f else animatedScore.coerceIn(0.0f, 305.0f)
		centerText("%.0f".format(score), 19.0f, 0.0f, 16.0f, EdenFeatures.dungeonScoreColor.argb)
	}

	private fun drawShitterAlert(preview: Boolean) {
		val pulse = if (preview) 0.5f else ((System.currentTimeMillis() % 700L) / 700.0f)
		val alpha = (205 + 50 * (1.0f - pulse)).toInt().coerceIn(0, 255)
		EdenNvgRenderer.roundedRect(0.0f, 0.0f, 88.0f, 24.0f, 6.0f, 0xD9150A0A.toInt())
		EdenNvgRenderer.roundedOutline(0.5f, 0.5f, 87.0f, 23.0f, 6.0f, 1.5f, withAlpha(0xFFFF3030.toInt(), alpha))
		centerText("NOT 4/4", 44.0f, 5.0f, 14.0f, withAlpha(0xFFFF5555.toInt(), alpha))
	}

	private data class PerformanceMetric(
		val label: String,
		val value: String,
		val history: List<Float>,
		val minimum: Float,
		val maximum: Float,
		val graphEnabled: Boolean,
		val graphColor: Int,
	)

	private fun drawPerformance(preview: Boolean) {
		val metrics = performanceMetrics(preview)
		val labelColor = 0xBFFFFFFF.toInt()
		val valueColor = 0xFFFFFFFF.toInt()
		if (metrics.isEmpty()) {
			if (preview) EdenNvgRenderer.text("PERFORMANCE HUD", 0.0f, 0.0f, 9.0f, valueColor)
			return
		}

		for ((index, metric) in metrics.withIndex()) {
			val y = index * 12.0f
			EdenNvgRenderer.text(metric.label, 0.0f, y + 1.0f, 8.0f, labelColor)
			EdenNvgRenderer.text(metric.value, 26.0f, y, 9.0f, valueColor)
			if (metric.graphEnabled) {
				drawGraph(
					metric.history,
					metric.minimum,
					metric.maximum,
					64.0f,
					y + 1.0f,
					68.0f,
					8.0f,
					metric.graphColor,
				)
			}
		}
	}

	private fun drawGraph(
		values: List<Float>,
		minimum: Float,
		maximum: Float,
		x: Float,
		y: Float,
		width: Float,
		height: Float,
		color: Int,
	) {
		EdenNvgRenderer.line(x, y + height, x + width, y + height, 0.6f, 0x30FFFFFF)
		if (values.size < 2) return
		val shown = values.takeLast(90)
		val step = width / (shown.size - 1).coerceAtLeast(1)
		for (index in 1 until shown.size) {
			val previous = ((shown[index - 1] - minimum) / (maximum - minimum)).coerceIn(0.0f, 1.0f)
			val current = ((shown[index] - minimum) / (maximum - minimum)).coerceIn(0.0f, 1.0f)
			EdenNvgRenderer.line(
				x + (index - 1) * step,
				y + height - previous * height,
				x + index * step,
				y + height - current * height,
				1.0f,
				color,
			)
		}
	}

	private fun performanceMetrics(preview: Boolean = false): List<PerformanceMetric> {
		val metrics = ArrayList<PerformanceMetric>(3)
		if (EdenFeatures.performanceHudShowTps) {
			metrics.add(
				PerformanceMetric(
					"TPS",
					String.format(java.util.Locale.ROOT, "%.1f", if (preview) 20.0f else PerformanceMetricsFeature.averageTps),
					previewHistory(PerformanceMetricsFeature.tpsHistory(), preview, listOf(20.0f, 19.9f, 18.2f, 19.8f, 20.0f)),
					0.0f,
					20.0f,
					EdenFeatures.performanceHudShowTpsGraph,
					0xFFFFFFFF.toInt(),
				),
			)
		}
		if (EdenFeatures.performanceHudShowFps) {
			metrics.add(
				PerformanceMetric(
					"FPS",
					"${if (preview) 144 else PerformanceMetricsFeature.fps()}fps",
					previewHistory(PerformanceMetricsFeature.fpsHistory(), preview, listOf(144.0f, 142.0f, 91.0f, 138.0f, 146.0f)),
					0.0f,
					240.0f,
					EdenFeatures.performanceHudShowFpsGraph,
					0xCCFFFFFF.toInt(),
				),
			)
		}
		if (EdenFeatures.performanceHudShowPing) {
			metrics.add(
				PerformanceMetric(
					"PING",
					"${if (preview) 28 else PerformanceMetricsFeature.averagePing}ms",
					previewHistory(PerformanceMetricsFeature.pingHistory(), preview, listOf(28.0f, 29.0f, 68.0f, 31.0f, 28.0f)),
					0.0f,
					250.0f,
					EdenFeatures.performanceHudShowPingGraph,
					0x99FFFFFF.toInt(),
				),
			)
		}
		return metrics
	}

	private fun previewHistory(actual: List<Float>, preview: Boolean, example: List<Float>): List<Float> =
		if (preview && actual.size < 2) example else actual

	private fun centerText(text: String, centerX: Float, y: Float, size: Float, color: Int) {
		val width = EdenNvgRenderer.textWidth(text, size)
		EdenNvgRenderer.text(text, centerX - width / 2.0f, y, size, color)
	}

	private fun compactNumber(value: Int): String = when {
		value >= 1_000_000 -> String.format(java.util.Locale.ROOT, "%.1fm", value / 1_000_000.0)
		value >= 10_000 -> String.format(java.util.Locale.ROOT, "%.1fk", value / 1_000.0)
		else -> "%,d".format(value)
	}

	private fun baseSize(widget: EdenHudWidget): Pair<Float, Float> {
		return when (widget) {
			EdenHudWidget.PET -> 150.0f to 14.0f
			EdenHudWidget.PRESSURE -> 34.0f to 44.0f
			EdenHudWidget.DRILL_FUEL -> 88.0f to 13.0f
			EdenHudWidget.DUNGEON_SCORE -> 38.0f to 18.0f
			EdenHudWidget.SHITTER_ALERT -> 88.0f to 24.0f
			EdenHudWidget.PERFORMANCE -> {
				val metrics = performanceMetrics()
				val count = metrics.size.coerceAtLeast(1)
				(if (metrics.any { it.graphEnabled }) 132.0f else 62.0f) to (count * 12.0f)
			}
		}
	}

	private fun anchor(widget: EdenHudWidget): Int = when (widget) {
		EdenHudWidget.PET -> 7
		EdenHudWidget.PRESSURE -> 7
		EdenHudWidget.DRILL_FUEL -> 7
		EdenHudWidget.DUNGEON_SCORE -> 7
		EdenHudWidget.SHITTER_ALERT -> 6
		EdenHudWidget.PERFORMANCE -> 0
	}

	private fun scale(widget: EdenHudWidget): Float = when (widget) {
		EdenHudWidget.PET -> EdenFeatures.petOverlayScale
		EdenHudWidget.PRESSURE -> EdenFeatures.pressureDisplayScale
		EdenHudWidget.DRILL_FUEL -> EdenFeatures.drillFuelMeterScale
		EdenHudWidget.DUNGEON_SCORE -> EdenFeatures.dungeonScoreMeterScale
		EdenHudWidget.SHITTER_ALERT -> EdenFeatures.shitterAlertScale
		EdenHudWidget.PERFORMANCE -> EdenFeatures.performanceHudScale
	}

	private fun offsetX(widget: EdenHudWidget): Float = when (widget) {
		EdenHudWidget.PET -> EdenFeatures.petOverlayX.toFloat()
		EdenHudWidget.PRESSURE -> EdenFeatures.pressureDisplayX.toFloat()
		EdenHudWidget.DRILL_FUEL -> EdenFeatures.drillFuelMeterX.toFloat()
		EdenHudWidget.DUNGEON_SCORE -> EdenFeatures.dungeonScoreMeterX.toFloat()
		EdenHudWidget.SHITTER_ALERT -> EdenFeatures.shitterAlertX.toFloat()
		EdenHudWidget.PERFORMANCE -> EdenFeatures.performanceHudX.toFloat()
	}

	private fun offsetY(widget: EdenHudWidget): Float = when (widget) {
		EdenHudWidget.PET -> EdenFeatures.petOverlayY.toFloat()
		EdenHudWidget.PRESSURE -> EdenFeatures.pressureDisplayY.toFloat()
		EdenHudWidget.DRILL_FUEL -> EdenFeatures.drillFuelMeterY.toFloat()
		EdenHudWidget.DUNGEON_SCORE -> EdenFeatures.dungeonScoreMeterY.toFloat()
		EdenHudWidget.SHITTER_ALERT -> EdenFeatures.shitterAlertY.toFloat()
		EdenHudWidget.PERFORMANCE -> EdenFeatures.performanceHudY.toFloat()
	}

	private fun anchoredPosition(
		anchor: Int,
		screenWidth: Float,
		screenHeight: Float,
		width: Float,
		height: Float,
	): Pair<Float, Float> {
		val margin = 4.0f
		return when (anchor.coerceIn(0, 7)) {
			0 -> margin to margin
			1 -> margin to (screenHeight - height) / 2.0f
			2 -> margin to screenHeight - height - margin
			3 -> screenWidth - width - margin to margin
			4 -> screenWidth - width - margin to (screenHeight - height) / 2.0f
			5 -> screenWidth - width - margin to screenHeight - height - margin
			6 -> (screenWidth - width) / 2.0f to margin
			else -> (screenWidth - width) / 2.0f to screenHeight - height - margin
		}
	}

	private fun lerp(current: Float, target: Float): Float = current + (target - current) * 0.09f

	private fun withAlpha(color: Int, alpha: Int): Int {
		return (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)
	}

}
