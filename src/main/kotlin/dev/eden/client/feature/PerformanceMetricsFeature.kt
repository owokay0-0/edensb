package dev.eden.client.feature

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import java.util.ArrayDeque
import kotlin.math.roundToInt

object PerformanceMetricsFeature {
	@Volatile
	var averageTps: Float = 20.0f
		private set

	@Volatile
	var averagePing: Int = 0
		private set

	private val pingSamples = ArrayDeque<Int>()
	private val fpsSamples = ArrayDeque<Float>()
	private val tpsGraphSamples = ArrayDeque<Float>()
	private val pingGraphSamples = ArrayDeque<Float>()
	private var previousTimePacketAt = 0L
	private var nextPingSampleAt = 0L
	private var lastFrameAt = 0L

	fun register() {
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			samplePing(client)
		}
		ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
			reset()
		}
	}

	@JvmStatic
	fun handleTimeUpdate() {
		val now = System.currentTimeMillis()
		if (previousTimePacketAt != 0L) {
			averageTps = (20_000.0f / (now - previousTimePacketAt + 1L)).coerceIn(0.0f, 20.0f)
			tpsGraphSamples.addLast(averageTps)
			while (tpsGraphSamples.size > 90) tpsGraphSamples.removeFirst()
		}
		previousTimePacketAt = now
	}

	fun fps(): Int = Minecraft.getInstance().fps

	fun recordFrame() {
		val now = System.nanoTime()
		if (lastFrameAt != 0L) {
			val frameMs = (now - lastFrameAt) / 1_000_000.0f
			if (frameMs in 1.0f..500.0f) {
				fpsSamples.addLast((1_000.0f / frameMs).coerceIn(0.0f, 240.0f))
				while (fpsSamples.size > 90) fpsSamples.removeFirst()
			}
		}
		lastFrameAt = now
	}

	fun fpsHistory(): List<Float> = fpsSamples.toList()

	fun tpsHistory(): List<Float> = tpsGraphSamples.toList()

	fun pingHistory(): List<Float> = pingGraphSamples.toList()

	private fun samplePing(client: Minecraft) {
		val now = System.currentTimeMillis()
		if (now < nextPingSampleAt) {
			return
		}
		nextPingSampleAt = now + 500L

		val player = client.player ?: return
		val ping = client.connection?.getPlayerInfo(player.uuid)?.latency?.coerceAtLeast(0) ?: return
		pingSamples.addLast(ping)
		while (pingSamples.size > 20) {
			pingSamples.removeFirst()
		}
		averagePing = pingSamples.average().roundToInt()
		pingGraphSamples.addLast(averagePing.toFloat())
		while (pingGraphSamples.size > 90) pingGraphSamples.removeFirst()
	}

	private fun reset() {
		averageTps = 20.0f
		averagePing = 0
		previousTimePacketAt = 0L
		nextPingSampleAt = 0L
		pingSamples.clear()
		fpsSamples.clear()
		tpsGraphSamples.clear()
		pingGraphSamples.clear()
		lastFrameAt = 0L
	}
}
