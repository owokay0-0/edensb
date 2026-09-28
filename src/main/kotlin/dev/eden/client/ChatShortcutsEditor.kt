package dev.eden.client

import com.mojang.blaze3d.platform.InputConstants
import io.github.notenoughupdates.moulconfig.GuiTextures
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent
import io.github.notenoughupdates.moulconfig.gui.editors.ComponentEditor
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import java.util.function.BiFunction
import java.util.function.Supplier

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigEditorChatShortcuts

class ChatShortcutsEditor(option: ProcessedOption) : ComponentEditor(option) {
	private val shortcuts = ChatShortcutsListComponent()
	private val delegate = wrapComponent(
		FixedButtonComponent(EDITOR_CONTENT_WIDTH - 150, Supplier { "New" }, Runnable(shortcuts::addShortcut)),
		shortcuts,
	)

	override fun getDelegate(): GuiComponent = delegate

	override fun setGuiContext(guiContext: GuiContext) {
		super.setGuiContext(guiContext)
		shortcuts.attachContext(guiContext)
	}

	private companion object {
		const val EDITOR_CONTENT_WIDTH = 420
	}
}

private class ChatShortcutsListComponent : GuiComponent() {
	private val shortcuts = EdenConfig.mutableChatShortcuts()
	private var rows = mutableListOf<ChatShortcutRowComponent>()
	private var capturing: EdenConfig.ChatShortcutData? = null

	init {
		rebuildRows()
	}

	override fun getWidth(): Int = 420

	override fun getHeight(): Int = if (rows.isEmpty()) EMPTY_HEIGHT else rows.size * ROW_HEIGHT

	override fun <T : Any?> foldChildren(initial: T, visitor: BiFunction<GuiComponent, T, T>): T {
		var state = initial
		for (row in rows) state = visitor.apply(row, state)
		return state
	}

	override fun render(context: GuiImmediateContext) {
		if (rows.isEmpty()) {
			context.renderContext.drawStringCenteredScaledMaxWidth(
				StructuredText.of("Press New to add a shortcut"),
				IMinecraft.INSTANCE.defaultFontRenderer,
				context.width / 2.0f,
				EMPTY_HEIGHT / 2.0f,
				false,
				context.width - 8,
				0xFF8D8B9A.toInt(),
			)
			return
		}

		context.renderContext.pushMatrix()
		forEachRow(context) { row, rowContext ->
			row.render(rowContext)
			context.renderContext.translate(0.0f, ROW_HEIGHT.toFloat())
		}
		context.renderContext.popMatrix()
	}

	override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
		if (mouseEvent is MouseEvent.Click && mouseEvent.mouseState) {
			val shortcut = capturing
			if (shortcut != null) {
				val sdlButton = MoulConfigInputCompat.toSdlMouseButton(mouseEvent.mouseButton)
				if (sdlButton in 1..32) {
					shortcut.keyName = InputConstants.Type.MOUSE.getOrCreate(sdlButton).name
					capturing = null
					return true
				}
			}
		}

		var handled = false
		forEachRow(context) { row, rowContext ->
			if (row.mouseEvent(mouseEvent, rowContext)) handled = true
		}
		return handled
	}

	override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
		val shortcut = capturing
		if (shortcut != null) {
			if (event is KeyboardEvent.KeyPressed && event.pressed) {
				val sdlScancode = MoulConfigInputCompat.toSdlScancode(event.keycode)
				if (sdlScancode > 0) {
					shortcut.keyName = InputConstants.Type.KEYBOARD.getOrCreate(sdlScancode).name
					capturing = null
				}
			}
			return true
		}

		var handled = false
		forEachRow(context) { row, rowContext ->
			if (row.keyboardEvent(event, rowContext)) handled = true
		}
		return handled
	}

	fun addShortcut() {
		if (capturing != null) return
		shortcuts.add(EdenConfig.ChatShortcutData())
		capturing = null
		rebuildRows()
	}

	fun attachContext(guiContext: GuiContext) {
		context = guiContext
		for (row in rows) row.attachContext(guiContext)
	}

	private fun beginBinding(shortcut: EdenConfig.ChatShortcutData) {
		capturing = shortcut
	}

	private fun removeShortcut(shortcut: EdenConfig.ChatShortcutData) {
		rows.firstOrNull { it.shortcut === shortcut }?.releaseFocus()
		shortcuts.remove(shortcut)
		if (capturing === shortcut) capturing = null
		rebuildRows()
	}

	private fun rebuildRows() {
		rows = shortcuts.mapTo(mutableListOf()) { shortcut ->
			ChatShortcutRowComponent(
				shortcut,
				Supplier { capturing === shortcut },
				Runnable { beginBinding(shortcut) },
				Runnable { removeShortcut(shortcut) },
			).also { row -> row.attachContext(context) }
		}
	}

	private inline fun forEachRow(
		context: GuiImmediateContext,
		block: (ChatShortcutRowComponent, GuiImmediateContext) -> Unit,
	) {
		for ((index, row) in rows.withIndex()) {
			block(row, context.translated(0, index * ROW_HEIGHT, context.width, ROW_HEIGHT))
		}
	}

	private companion object {
		const val ROW_HEIGHT = 22
		const val EMPTY_HEIGHT = 24
	}
}

private class ChatShortcutRowComponent(
	val shortcut: EdenConfig.ChatShortcutData,
	private val capturing: Supplier<Boolean>,
	beginBinding: Runnable,
	remove: Runnable,
) : GuiComponent() {
	private val textField = TextFieldComponent(
		object : GetSetter<String> {
			override fun get(): String = shortcut.message
			override fun set(newValue: String) {
				shortcut.message = newValue.take(256)
			}
		},
		240,
		GetSetter.constant(true),
		"/ld, /wardrobe, hi...",
	)
	private val bindButton = FixedButtonComponent(BIND_WIDTH, Supplier(::bindingText), beginBinding)
	private val removeButton = FixedButtonComponent(REMOVE_WIDTH, Supplier { "×" }, remove)

	override fun getWidth(): Int = 420
	override fun getHeight(): Int = ROW_HEIGHT

	override fun <T : Any?> foldChildren(initial: T, visitor: BiFunction<GuiComponent, T, T>): T {
		var state = visitor.apply(textField, initial)
		state = visitor.apply(bindButton, state)
		return visitor.apply(removeButton, state)
	}

	override fun render(context: GuiImmediateContext) {
		val textWidth = textWidth(context)
		context.renderContext.pushMatrix()
		context.renderContext.translate(0.0f, 2.0f)
		textField.render(context.translated(0, 2, textWidth, FIELD_HEIGHT))
		context.renderContext.translate((textWidth + GAP).toFloat(), 0.0f)
		bindButton.render(context.translated(textWidth + GAP, 2, BIND_WIDTH, FIELD_HEIGHT))
		context.renderContext.translate((BIND_WIDTH + GAP).toFloat(), 0.0f)
		removeButton.render(context.translated(textWidth + GAP + BIND_WIDTH + GAP, 2, REMOVE_WIDTH, FIELD_HEIGHT))
		context.renderContext.popMatrix()
	}

	override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
		val textWidth = textWidth(context)
		if (textField.mouseEvent(mouseEvent, context.translated(0, 2, textWidth, FIELD_HEIGHT))) return true
		if (bindButton.mouseEvent(mouseEvent, context.translated(textWidth + GAP, 2, BIND_WIDTH, FIELD_HEIGHT))) return true
		return removeButton.mouseEvent(
			mouseEvent,
			context.translated(textWidth + GAP + BIND_WIDTH + GAP, 2, REMOVE_WIDTH, FIELD_HEIGHT),
		)
	}

	override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
		return textField.keyboardEvent(event, context.translated(0, 2, textWidth(context), FIELD_HEIGHT))
	}

	fun attachContext(guiContext: io.github.notenoughupdates.moulconfig.gui.GuiContext?) {
		if (guiContext == null) return
		context = guiContext
		textField.context = guiContext
		bindButton.context = guiContext
		removeButton.context = guiContext
	}

	fun releaseFocus() {
		textField.blur()
	}

	private fun textWidth(context: GuiImmediateContext): Int {
		return (context.width - BIND_WIDTH - REMOVE_WIDTH - GAP * 2).coerceAtLeast(80)
	}

	private fun bindingText(): String {
		if (capturing.get()) return "Press a key..."
		val keyName = shortcut.keyName ?: return "Set"
		val displayName = runCatching { InputConstants.getKey(keyName).displayName.string }.getOrNull()
		return "Set: ${displayName ?: "Unknown"}"
	}

	private companion object {
		const val ROW_HEIGHT = 22
		const val FIELD_HEIGHT = 18
		const val BIND_WIDTH = 112
		const val REMOVE_WIDTH = 22
		const val GAP = 4
	}
}

private class FixedButtonComponent(
	private val requestedWidth: Int,
	private val label: Supplier<String>,
	private val onClick: Runnable,
) : GuiComponent() {
	override fun getWidth(): Int = requestedWidth
	override fun getHeight(): Int = 18

	override fun render(context: GuiImmediateContext) {
		context.renderContext.drawTexturedRect(
			GuiTextures.BUTTON,
			0.0f,
			0.0f,
			context.width.toFloat(),
			context.height.toFloat(),
		)
		context.renderContext.drawStringCenteredScaledMaxWidth(
			StructuredText.of(label.get()),
			IMinecraft.INSTANCE.defaultFontRenderer,
			context.width / 2.0f,
			context.height / 2.0f,
			false,
			context.width - 4,
			0xFF000000.toInt(),
		)
	}

	override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
		if (context.isHovered && mouseEvent is MouseEvent.Click && mouseEvent.mouseState && mouseEvent.mouseButton == 0) {
			onClick.run()
			return true
		}
		return false
	}
}
