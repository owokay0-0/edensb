package dev.eden.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.eden.client.MoulConfigInputCompat;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigPlatform;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigText;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = MoulConfigPlatform.class, remap = false)
public abstract class MoulConfigPlatformCompatMixin {
	@Overwrite(remap = false)
	public boolean isKeyboardKeyDown(int keyCode) {
		int sdlScancode = MoulConfigInputCompat.toSdlScancode(keyCode);
		return sdlScancode >= 0 && InputConstants.isKeyDown(sdlScancode);
	}

	@Overwrite(remap = false)
	public boolean isMouseButtonDown(int glfwButton) {
		int sdlButton = MoulConfigInputCompat.toSdlMouseButton(glfwButton);
		if (sdlButton < 1 || sdlButton > 32) {
			return false;
		}
		int mask = 1 << (sdlButton - 1);
		return (SDLMouse.SDL_GetMouseState(null, null) & mask) != 0;
	}

	@Overwrite(remap = false)
	public StructuredText getKeyName(int keyCode) {
		if (keyCode == -1) {
			return StructuredText.of("NONE");
		}

		Component displayName;
		if (keyCode >= 0 && keyCode < 32) {
			int sdlButton = MoulConfigInputCompat.toSdlMouseButton(keyCode);
			displayName = InputConstants.Type.MOUSE.getOrCreate(sdlButton).getDisplayName();
		} else {
			int sdlScancode = MoulConfigInputCompat.toSdlScancode(keyCode);
			if (sdlScancode < 0) {
				return StructuredText.of("???");
			}
			displayName = InputConstants.Type.KEYBOARD.getOrCreate(sdlScancode).getDisplayName();
		}
		return MoulConfigText.wrap(displayName);
	}
}
