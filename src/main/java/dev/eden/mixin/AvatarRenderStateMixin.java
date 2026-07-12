package dev.eden.mixin;

import dev.eden.client.render.AvatarRenderStateExt;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateExt {
	@Unique
	private boolean eden$playerHiderHidden;

	@Unique
	private boolean eden$playerHiderGhost;

	@Unique
	private boolean eden$playerSizeScale;

	@Override
	public boolean eden$isPlayerHiderHidden() {
		return this.eden$playerHiderHidden;
	}

	@Override
	public void eden$setPlayerHiderHidden(boolean hidden) {
		this.eden$playerHiderHidden = hidden;
	}

	@Override
	public boolean eden$isPlayerHiderGhost() {
		return this.eden$playerHiderGhost;
	}

	@Override
	public void eden$setPlayerHiderGhost(boolean ghost) {
		this.eden$playerHiderGhost = ghost;
	}

	@Override
	public boolean eden$shouldPlayerSizeScale() {
		return this.eden$playerSizeScale;
	}

	@Override
	public void eden$setPlayerSizeScale(boolean shouldScale) {
		this.eden$playerSizeScale = shouldScale;
	}
}
