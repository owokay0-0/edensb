package dev.eden.client.feature

object FullbrightFeature {
	@JvmStatic
	fun isActive(): Boolean = EdenFeatures.fullbrightEnabled
}
