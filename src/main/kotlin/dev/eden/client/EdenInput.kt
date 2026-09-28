package dev.eden.client

import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.sdl.SDLMouse

object EdenInput {
	fun isDown(keyName: String): Boolean {
		val key = runCatching { InputConstants.getKey(keyName) }.getOrNull() ?: return false
		return when (key.type) {
			InputConstants.Type.KEYBOARD -> InputConstants.isKeyDown(key.value)
			InputConstants.Type.MOUSE -> isMouseButtonDown(key.value)
		}
	}

	private fun isMouseButtonDown(button: Int): Boolean {
		if (button !in 1..32) {
			return false
		}
		val mask = 1 shl (button - 1)
		return SDLMouse.SDL_GetMouseState(null, null) and mask != 0
	}
}
