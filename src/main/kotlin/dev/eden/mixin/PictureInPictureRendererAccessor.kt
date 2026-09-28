package dev.eden.mixin

import com.mojang.renderpearl.api.textures.GpuTextureView
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(PictureInPictureRenderer::class)
interface PictureInPictureRendererAccessor {
	@Accessor("textureView")
	fun edenColorTextureView(): GpuTextureView?

	@Accessor("depthTextureView")
	fun edenDepthTextureView(): GpuTextureView?
}
