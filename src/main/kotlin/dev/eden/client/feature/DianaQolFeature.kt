package dev.eden.client.feature

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.BlockState

object DianaQolFeature {
	private val grassBlockIds = setOf(
		"minecraft:short_grass",
		"minecraft:tall_grass",
		"minecraft:fern",
		"minecraft:large_fern",
	)
	private val bushBlockIds = setOf(
		"minecraft:dead_bush",
		"minecraft:bush",
	)
	private val flowerBlockIds = setOf(
		"minecraft:red_tulip",
		"minecraft:azure_bluet",
		"minecraft:rose",
	)

	@JvmStatic
	fun isActive(): Boolean {
		return EdenFeatures.dianaQolEnabled && SkyblockDataTracker.shouldRender()
	}

	@JvmStatic
	fun shouldIgnore(state: BlockState): Boolean {
		if (!isActive()) return false
		val id = BuiltInRegistries.BLOCK.getKey(state.block).toString()
		return (EdenFeatures.dianaIgnoreGrass && id in grassBlockIds) ||
			(EdenFeatures.dianaIgnoreFlowers && id in flowerBlockIds) ||
			(EdenFeatures.dianaIgnoreBushes && id in bushBlockIds)
	}
}
