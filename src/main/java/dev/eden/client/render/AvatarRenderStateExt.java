package dev.eden.client.render;

public interface AvatarRenderStateExt {
	boolean eden$isPlayerHiderHidden();

	void eden$setPlayerHiderHidden(boolean hidden);

	boolean eden$isPlayerHiderGhost();

	void eden$setPlayerHiderGhost(boolean ghost);

	boolean eden$shouldPlayerSizeScale();

	void eden$setPlayerSizeScale(boolean shouldScale);
}
