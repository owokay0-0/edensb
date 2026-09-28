package dev.eden.mixin;

import dev.eden.client.MoulConfigInputCompat;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext;
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent;
import io.github.notenoughupdates.moulconfig.gui.MouseEvent;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = MoulConfigScreenComponent.class, remap = false)
public abstract class MoulConfigScreenInputCompatMixin {
	@Shadow @Final
	private GuiContext guiContext;

	@Shadow
	public abstract GuiImmediateContext createContext();

	@Shadow
	public abstract void onClose();

	@Unique
	private double eden$lastMouseX = Double.NaN;

	@Unique
	private double eden$lastMouseY = Double.NaN;

	@Unique
	private GuiImmediateContext eden$contextAt(double x, double y) {
		GuiImmediateContext base = createContext();
		int mouseX = (int) x;
		int mouseY = (int) y;
		return new GuiImmediateContext(
			base.getRenderContext(),
			base.getRenderOffsetX(),
			base.getRenderOffsetY(),
			base.getWidth(),
			base.getHeight(),
			mouseX,
			mouseY,
			mouseX,
			mouseY,
			(float) x,
			(float) y
		);
	}

	@Overwrite(remap = false)
	public boolean charTyped(CharacterEvent event) {
		return guiContext.getRoot().keyboardEvent(
			new KeyboardEvent.CharTyped((char) event.codepoint()),
			createContext()
		);
	}

	@Overwrite(remap = false)
	public boolean keyPressed(KeyEvent event) {
		int glfwKey = MoulConfigInputCompat.toGlfwKeycode(event.keycode(), event.key());
		int glfwScancode = MoulConfigInputCompat.toGlfwKey(event.key());
		if (glfwKey == 256) {
			onClose();
			return true;
		}
		return guiContext.getRoot().keyboardEvent(
			new KeyboardEvent.KeyPressed(glfwKey, glfwScancode, true),
			createContext()
		);
	}

	@Overwrite(remap = false)
	public boolean keyReleased(KeyEvent event) {
		return guiContext.getRoot().keyboardEvent(
			new KeyboardEvent.KeyPressed(
				MoulConfigInputCompat.toGlfwKeycode(event.keycode(), event.key()),
				MoulConfigInputCompat.toGlfwKey(event.key()),
				false
			),
			createContext()
		);
	}

	@Overwrite(remap = false)
	public void mouseMoved(double x, double y) {
		float deltaX = Double.isNaN(eden$lastMouseX) ? 0.0F : (float) (x - eden$lastMouseX);
		float deltaY = Double.isNaN(eden$lastMouseY) ? 0.0F : (float) (y - eden$lastMouseY);
		eden$lastMouseX = x;
		eden$lastMouseY = y;
		guiContext.getRoot().mouseEvent(new MouseEvent.Move(deltaX, deltaY), eden$contextAt(x, y));
	}

	@Overwrite(remap = false)
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return guiContext.getRoot().mouseEvent(
			new MouseEvent.Click(MoulConfigInputCompat.toGlfwMouseButton(event.button()), true),
			eden$contextAt(event.x(), event.y())
		);
	}

	@Overwrite(remap = false)
	public boolean mouseReleased(MouseButtonEvent event) {
		return guiContext.getRoot().mouseEvent(
			new MouseEvent.Click(MoulConfigInputCompat.toGlfwMouseButton(event.button()), false),
			eden$contextAt(event.x(), event.y())
		);
	}

	@Overwrite(remap = false)
	public boolean mouseScrolled(double x, double y, double horizontalAmount, double verticalAmount) {
		return guiContext.getRoot().mouseEvent(
			new MouseEvent.Scroll((float) verticalAmount),
			eden$contextAt(x, y)
		);
	}

	@Overwrite(remap = false)
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		return true;
	}
}
