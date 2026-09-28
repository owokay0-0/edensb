package dev.eden.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "io.github.notenoughupdates.moulconfig.platform.MoulConfigRenderContext$1", remap = false)
public abstract class MoulConfigGuiElementRenderStateCompatMixin implements GuiElementRenderState {
	@Override
	public RenderPipeline pipeline() {
		return RenderPipelines.GUI;
	}
}
