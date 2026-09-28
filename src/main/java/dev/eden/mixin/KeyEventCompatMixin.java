package dev.eden.mixin;

import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(KeyEvent.class)
public abstract class KeyEventCompatMixin {
	@Shadow
	public abstract int key();

	@Unique
	public int scancode() {
		return key();
	}
}
