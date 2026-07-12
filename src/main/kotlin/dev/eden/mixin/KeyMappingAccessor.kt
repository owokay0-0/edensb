package dev.eden.mixin

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(KeyMapping::class)
interface KeyMappingAccessor {
	@Accessor("key")
	fun edenBoundKey(): InputConstants.Key

	@Accessor("clickCount")
	fun edenClickCount(): Int

	@Accessor("clickCount")
	fun edenSetClickCount(count: Int)
}
