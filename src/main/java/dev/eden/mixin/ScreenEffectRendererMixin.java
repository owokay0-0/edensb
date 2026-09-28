package dev.eden.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eden.client.feature.RenderOptimizerFeature;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
	@Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
	private static void eden$hideFireOverlay(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		TextureAtlasSprite sprite,
		CallbackInfo ci
	) {
		if (RenderOptimizerFeature.shouldHideFireOverlay()) {
			ci.cancel();
		}
	}
}
