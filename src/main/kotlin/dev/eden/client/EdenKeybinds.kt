package dev.eden.client

import dev.eden.client.feature.EdenFeatures
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object EdenKeybinds {
	private val pressed = HashMap<String, Boolean>()
	private val shortcutPressed = HashMap<String, Boolean>()
	private val moduleDefaults = mapOf(
		"Dungeons.Etherwarp" to false,
		"Dungeons.DungeonBreaker" to false,
		"Dungeons.Teammate Highlight" to false,
	)
	private val moduleNames = mapOf(
		"Dungeons.Etherwarp" to "Etherwarp",
		"Dungeons.DungeonBreaker" to "DungeonBreaker",
		"Dungeons.Teammate Highlight" to "Teammate Highlight",
	)
	private val defaultKeybinds = emptyMap<String, String>()

	fun tick(client: Minecraft) {
		val bindings = currentBindings()
		val allowInput = client.gui.screen() == null
		for ((moduleKey, keyName) in bindings) {
			val down = EdenInput.isDown(keyName)
			val id = "$moduleKey:$keyName"
			if (allowInput && down && pressed[id] != true) {
				toggleModule(client, moduleKey)
			}
			pressed[id] = down
		}

		val activeIds = bindings.mapTo(HashSet()) { "${it.key}:${it.value}" }
		pressed.keys.retainAll(activeIds)

		val shortcuts = EdenConfig.chatShortcuts()
		for ((index, shortcut) in shortcuts.withIndex()) {
			val keyName = shortcut.keyName ?: continue
			val down = EdenInput.isDown(keyName)
			val id = "$index:$keyName"
			if (allowInput && down && shortcutPressed[id] != true) {
				runChatShortcut(client, shortcut.message)
			}
			shortcutPressed[id] = down
		}
		val activeShortcutIds = shortcuts.mapIndexedNotNullTo(HashSet()) { index, shortcut ->
			shortcut.keyName?.let { "$index:$it" }
		}
		shortcutPressed.keys.retainAll(activeShortcutIds)
	}

	private fun runChatShortcut(client: Minecraft, configuredMessage: String) {
		val message = configuredMessage.trim()
		if (message.isEmpty()) return
		if (message.startsWith('/')) {
			val command = message.drop(1).trim()
			if (command.isNotEmpty()) client.connection?.sendCommand(command)
		} else {
			client.connection?.sendChat(message)
		}
	}

	private fun currentBindings(): Map<String, String> {
		val bindings = LinkedHashMap(defaultKeybinds)
		for ((entryKey, entry) in EdenConfig.entriesSnapshot()) {
			if (!entryKey.endsWith(".Keybind")) {
				continue
			}
			val moduleKey = entryKey.removeSuffix(".Keybind")
			if (moduleKey !in moduleDefaults) {
				continue
			}
			val keyName = entry.keyName ?: inferKeyName(entry.value.orEmpty())
			if (keyName == null) {
				bindings.remove(moduleKey)
				continue
			}
			if (keyName.isNotBlank()) {
				bindings[moduleKey] = keyName
			}
		}
		return bindings
	}

	private fun toggleModule(client: Minecraft, moduleKey: String) {
		val current = EdenConfig.entry(moduleKey)?.enabled ?: moduleDefaults[moduleKey] ?: false
		val enabled = !current
		EdenConfig.updateEntry(moduleKey) {
			it.enabled = enabled
		}
		EdenFeatures.loadFromConfig()
		showToggleMessage(client, moduleNames[moduleKey] ?: moduleKey.substringAfterLast('.'), enabled)
	}

	private fun showToggleMessage(client: Minecraft, moduleName: String, enabled: Boolean) {
		val message = Component.literal("Eden")
			.withColor(EDEN_ACCENT)
			.append(Component.literal(" \u00BB ").withColor(MESSAGE_MUTED))
			.append(Component.literal(moduleName).withColor(MESSAGE_TEXT))
			.append(Component.literal(" "))
			.append(Component.literal(if (enabled) "enabled" else "disabled").withColor(if (enabled) ENABLED else DISABLED))
			.append(Component.literal(".").withColor(MESSAGE_TEXT))
		client.gui.hud.chat.addClientSystemMessage(message)
	}

	fun inferKeyName(displayValue: String): String? {
		val display = displayValue.removeSuffix(" X").trim()
		if (display.isBlank() || display.equals("n/a", ignoreCase = true) || display.equals("none", ignoreCase = true)) {
			return null
		}
		if (display.length == 1 && display[0].isLetterOrDigit()) {
			return "key.keyboard.${display.lowercase()}"
		}
		return null
	}

	private const val EDEN_ACCENT = 0xAAA4FF
	private const val MESSAGE_MUTED = 0x8D8B9A
	private const val MESSAGE_TEXT = 0xE7E5F1
	private const val ENABLED = 0x55FF55
	private const val DISABLED = 0xFF5555
}
