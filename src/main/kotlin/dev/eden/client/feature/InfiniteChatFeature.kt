package dev.eden.client.feature

object InfiniteChatFeature {
	const val CHAT_LIMIT: Int = 10_000

	@JvmStatic
	fun isActive(): Boolean = EdenFeatures.infiniteChatEnabled
}
