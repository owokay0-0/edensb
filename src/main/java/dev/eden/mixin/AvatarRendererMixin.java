package dev.eden.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eden.client.feature.PlayerHiderFeature;
import dev.eden.client.feature.PlayerSizeFeature;
import dev.eden.client.render.AvatarRenderStateExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
		at = @At("TAIL")
	)
	private void eden$prepareAvatarFeatures(
		Avatar renderedPlayer,
		AvatarRenderState renderState,
		float tickDelta,
		CallbackInfo ci
	) {
		AvatarRenderStateExt state = (AvatarRenderStateExt)renderState;
		state.eden$setPlayerHiderHidden(false);
		state.eden$setPlayerHiderGhost(false);

		boolean shouldScale = PlayerSizeFeature.shouldScale(renderedPlayer);
		state.eden$setPlayerSizeScale(shouldScale);
		PlayerSizeFeature.adjustNameTag(renderState, shouldScale);

		if (!PlayerHiderFeature.shouldHide(
			Minecraft.getInstance().player,
			renderedPlayer,
			renderState.x,
			renderState.y,
			renderState.z
		)) {
			return;
		}

		boolean ghost = PlayerHiderFeature.shouldRenderAsGhost(
			Minecraft.getInstance().player,
			renderedPlayer,
			renderState.x,
			renderState.y,
			renderState.z
		);
		state.eden$setPlayerHiderHidden(true);
		state.eden$setPlayerHiderGhost(ghost);
		renderState.isInvisible = !ghost;
		renderState.isInvisibleToPlayer = !ghost;
		renderState.showCape = false;
		renderState.nameTag = null;
		renderState.scoreText = null;
	}

	@Inject(
		method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
		at = @At("TAIL")
	)
	private void eden$scalePlayerModel(AvatarRenderState renderState, PoseStack poseStack, CallbackInfo ci) {
		if (((AvatarRenderStateExt)renderState).eden$shouldPlayerSizeScale()) {
			PlayerSizeFeature.applyScale(renderState, poseStack);
		}
	}

	@Inject(
		method = "shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void eden$hidePlayerLayers(
		AvatarRenderState renderState,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (((AvatarRenderStateExt)renderState).eden$isPlayerHiderHidden()) {
			cir.setReturnValue(false);
		}
	}
}
