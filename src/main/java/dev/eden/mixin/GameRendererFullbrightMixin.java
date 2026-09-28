package dev.eden.mixin;

import dev.eden.client.feature.FullbrightFeature;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererFullbrightMixin {
	@Inject(method = "bossOverlayWorldDarkening", at = @At("RETURN"), cancellable = true)
	private void eden$disableWorldDarkening(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (FullbrightFeature.isActive()) {
			cir.setReturnValue(0.0F);
		}
	}
}
