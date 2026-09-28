package dev.eden.client.feature

import dev.eden.client.EdenConfig
import dev.eden.client.EdenInput
import dev.eden.client.EdenKeybinds
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import kotlin.math.roundToInt
import kotlin.math.sign

object ZoomFeature {
	private const val MIN_INTENSITY = 1
	private const val MAX_INTENSITY = 10
	private const val MIN_ZOOM_FOV = 1
	private const val MAX_ZOOM_FOV = 50
	private const val DEFAULT_KEY = "key.keyboard.c"

	private var applied = false
	private var savedFov = 70

	fun register() {
		ClientTickEvents.END_CLIENT_TICK.register { client -> tick(client) }
	}

	@JvmStatic
	fun isZooming(): Boolean {
		val client = Minecraft.getInstance()
		return EdenFeatures.zoomEnabled &&
			client.gui.screen() == null &&
			client.isWindowActive &&
			isZoomKeyDown(client)
	}

	@JvmStatic
	fun adjustByScroll(scrollAmount: Double) {
		if (!EdenFeatures.zoomScrollable || !isZooming() || scrollAmount == 0.0) {
			return
		}

		val nextIntensity = (EdenFeatures.zoomIntensity + sign(scrollAmount).toInt())
			.coerceIn(MIN_INTENSITY, MAX_INTENSITY)
		if (nextIntensity == EdenFeatures.zoomIntensity) {
			return
		}

		EdenFeatures.zoomIntensity = nextIntensity
		EdenConfig.updateEntry("Visuals.Zoom.Intensity") {
			it.value = nextIntensity.toString()
			it.sliderPercentage = (nextIntensity - MIN_INTENSITY).toFloat() / (MAX_INTENSITY - MIN_INTENSITY)
		}
	}

	private fun tick(client: Minecraft) {
		if (isZooming()) {
			if (!applied) {
				savedFov = client.options.fov().get()
				applied = true
			}
			client.options.fov().set(fovForIntensity(EdenFeatures.zoomIntensity))
		} else if (applied) {
			client.options.fov().set(savedFov)
			applied = false
		}
	}

	private fun fovForIntensity(intensity: Int): Int {
		val clamped = intensity.coerceIn(MIN_INTENSITY, MAX_INTENSITY)
		val fov = MAX_ZOOM_FOV - (clamped - MIN_INTENSITY) * (MAX_ZOOM_FOV - MIN_ZOOM_FOV).toDouble() /
			(MAX_INTENSITY - MIN_INTENSITY)
		return fov.roundToInt().coerceIn(MIN_ZOOM_FOV, MAX_ZOOM_FOV)
	}

	private fun isZoomKeyDown(client: Minecraft): Boolean {
		val entry = EdenConfig.entry("Visuals.Zoom.Keybind")
		val keyName = entry?.keyName ?: EdenKeybinds.inferKeyName(entry?.value.orEmpty()) ?: DEFAULT_KEY
		return EdenInput.isDown(keyName)
	}
}
