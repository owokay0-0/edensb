package dev.eden.client.feature

object HitColorFeature {
	private const val VANILLA_HIT_COLOR = 0xB2FF0000.toInt()
	private var updateCallback: Runnable = Runnable {}

	@JvmStatic
	fun installUpdateCallback(callback: Runnable) {
		updateCallback = callback
	}

	@JvmStatic
	fun refresh() {
		updateCallback.run()
	}

	@JvmStatic
	fun overlayColor(): Int {
		return if (EdenFeatures.hitColorEnabled) EdenFeatures.hitColor.argb else VANILLA_HIT_COLOR
	}
}
