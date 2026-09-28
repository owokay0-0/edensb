package dev.eden.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation;
import io.github.notenoughupdates.moulconfig.common.TextureFilter;
import io.github.notenoughupdates.moulconfig.internal.FilterAssertionCache;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigPlatform;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MoulConfigRenderContext.class, remap = false)
public abstract class MoulConfigRenderContextCompatMixin {
	@Shadow @Final
	private GuiGraphicsExtractor drawContext;

	@Shadow
	private Minecraft mc;

	@Overwrite(remap = false)
	public void drawTexturedTintedRect(
		MyResourceLocation resource,
		float x,
		float y,
		float width,
		float height,
		float minU,
		float minV,
		float maxU,
		float maxV,
		int color,
		TextureFilter textureFilter
	) {
		FilterAssertionCache.assertTextureFilter(resource, textureFilter);
		Identifier id = MoulConfigPlatform.unwrap(resource);
		FilterMode filterMode = textureFilter == TextureFilter.LINEAR ? FilterMode.LINEAR : FilterMode.NEAREST;
		drawContext.innerBlit(
			RenderPipelines.GUI_TEXTURED,
			mc.getTextureManager().getTexture(id).getTextureView(),
			RenderSystem.getSamplerCache().getRepeat(filterMode),
			(int) x,
			(int) y,
			(int) (x + width),
			(int) (y + height),
			minU,
			maxU,
			minV,
			maxV,
			color
		);
	}
}
