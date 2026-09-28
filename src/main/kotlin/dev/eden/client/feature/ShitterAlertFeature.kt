package dev.eden.client.feature

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft

object ShitterAlertFeature {
	private val rolePattern = Regex(
		"""(?:\[(?:A|B|T|M|H)]|(?:Archer|Berserk|Tank|Mage|Healer)(?:\s+(?:\d+|[IVXLCDM]+))?)""",
		RegexOption.IGNORE_CASE,
	)
	private val failurePattern = Regex(
		"""(?:^|\s)(?:☠\s*)?[A-Za-z0-9_]{1,16}.*(?:became a ghost|disconnected from the dungeon|left the dungeon|was killed|died)""",
		RegexOption.IGNORE_CASE,
	)

	private var alertUntil = 0L
	private var dungeonStartedAt = 0L
	private var largestRoster = 0
	private var previousRoster = 0

	fun register() {
		ClientReceiveMessageEvents.GAME.register { message, overlay ->
			if (!overlay && EdenFeatures.shitterAlertEnabled && SkyblockDataTracker.inDungeon) {
				val text = ChatFormatting.stripFormatting(message.string).orEmpty()
				if (failurePattern.containsMatchIn(text)) trigger()
			}
		}
		ClientTickEvents.END_CLIENT_TICK.register(::tick)
		ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
	}

	fun visible(now: Long = System.currentTimeMillis()): Boolean = now < alertUntil

	private fun tick(client: Minecraft) {
		if (!SkyblockDataTracker.inDungeon) {
			resetRoster()
			return
		}

		val now = System.currentTimeMillis()
		if (dungeonStartedAt == 0L) dungeonStartedAt = now
		if (now - dungeonStartedAt < 5_000L) return

		val roster = dungeonRosterSize(client)
		if (roster >= 4) largestRoster = maxOf(largestRoster, roster)
		if (largestRoster >= 4 && previousRoster >= largestRoster && roster in 1 until largestRoster) {
			trigger()
		}
		previousRoster = roster
	}

	private fun dungeonRosterSize(client: Minecraft): Int {
		val connection = client.connection ?: return 0
		val ownName = client.player?.scoreboardName.orEmpty()
		return connection.onlinePlayers
			.mapNotNull { it.tabListDisplayName?.string }
			.map { ChatFormatting.stripFormatting(it).orEmpty() }
			.count { line ->
				rolePattern.containsMatchIn(line) &&
					(ownName.isBlank() || !line.contains(ownName, ignoreCase = true))
			}
	}

	private fun trigger() {
		alertUntil = maxOf(alertUntil, System.currentTimeMillis() + 4_000L)
	}

	private fun resetRoster() {
		dungeonStartedAt = 0L
		largestRoster = 0
		previousRoster = 0
	}

	private fun reset() {
		alertUntil = 0L
		resetRoster()
	}
}
