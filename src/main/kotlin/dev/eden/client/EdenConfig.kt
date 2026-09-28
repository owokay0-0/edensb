package dev.eden.client

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.math.roundToInt

object EdenConfig {
	private val gson = GsonBuilder().setPrettyPrinting().create()
	private val path: Path
		get() = FabricLoader.getInstance().configDir.resolve("eden.json")

	private var data = ConfigData()
	private var saveDeferralDepth = 0
	private var savePending = false
	private val entries: MutableMap<String, EntryData>
		get() {
			data.sanitize()
			return data.entries!!
		}
	private val columns: MutableMap<String, ColumnData>
		get() {
			data.sanitize()
			return data.columns!!
		}

	fun load() {
		val configPath = path
		data = if (configPath.exists()) {
			runCatching {
				configPath.reader().use { gson.fromJson(it, ConfigData::class.java) }
			}.getOrNull() ?: ConfigData()
		} else {
			ConfigData()
		}
		data.sanitize()
	}

	fun save() {
		if (saveDeferralDepth > 0) {
			savePending = true
			return
		}
		val configPath = path
		configPath.parent?.createDirectories()
		configPath.writeText(gson.toJson(data))
	}

	fun batchUpdate(block: () -> Unit) {
		saveDeferralDepth++
		try {
			block()
		} finally {
			saveDeferralDepth--
			if (saveDeferralDepth == 0 && savePending) {
				savePending = false
				save()
			}
		}
	}

	fun entry(key: String): EntryData? = entries[key]

	fun entriesSnapshot(): Map<String, EntryData> = entries.toMap()

	fun migrateEntryPrefix(oldPrefix: String, newPrefix: String) {
		migrateEntryPrefixes(oldPrefix to newPrefix)
	}

	fun migrateEntryPrefixes(vararg migrations: Pair<String, String>) {
		var changed = false
		for ((oldPrefix, newPrefix) in migrations) {
			val oldEntries = entries.filterKeys { it == oldPrefix || it.startsWith("$oldPrefix.") }
			for ((oldKey, entry) in oldEntries) {
				val newKey = newPrefix + oldKey.removePrefix(oldPrefix)
				entries.putIfAbsent(newKey, entry)
				entries.remove(oldKey)
				changed = true
			}
		}
		if (changed) {
			save()
		}
	}

	fun removeEntryPrefix(prefix: String) {
		val removed = entries.keys.removeIf { it == prefix || it.startsWith("$prefix.") }
		if (removed) {
			save()
		}
	}

	fun migrateEtherwarpLeftClickMode() {
		val modeKey = "Dungeons.Etherwarp.Left Click Mode"
		val leftClickKey = "Dungeons.Etherwarp.Left click etherwarp"
		val autoShiftKey = "Dungeons.Etherwarp.Shift automatically"
		val leftClick = entries.remove(leftClickKey)?.switchValue
		val autoShift = entries.remove(autoShiftKey)?.switchValue
		if (leftClick == null && autoShift == null) {
			return
		}
		val mode = when {
			leftClick == false -> 0
			autoShift == true -> 2
			else -> 1
		}
		entries.getOrPut(modeKey) { EntryData() }.selected = mode
		save()
	}

	fun migrateZoomIntensity() {
		val oldEntry = entries["Visuals.Zoom.FOV"] ?: return
		if (entries.containsKey("Visuals.Zoom.Intensity")) {
			entries.remove("Visuals.Zoom.FOV")
			save()
			return
		}
		val oldFov = oldEntry.value?.toFloatOrNull()
			?: (10.0f + 100.0f * (oldEntry.sliderPercentage ?: 0.2f).coerceIn(0.0f, 1.0f))
		val intensity = (1.0f + (50.0f - oldFov.coerceIn(1.0f, 50.0f)) * 9.0f / 49.0f)
			.roundToInt()
			.coerceIn(1, 10)
		entries.remove("Visuals.Zoom.FOV")
		entries["Visuals.Zoom.Intensity"] = EntryData().apply {
			value = intensity.toString()
			sliderPercentage = (intensity - 1).toFloat() / 9.0f
		}
		save()
	}

	fun migrateChatShortcuts() {
		val commandEntry = entries.remove("General.Chat Command.Command")
		val keyEntry = entries.remove("General.Chat Command.Keybind")
		if (commandEntry == null && keyEntry == null) return
		val command = commandEntry?.value.orEmpty()
		val keyName = keyEntry?.keyName
		if (data.chatShortcuts.isNullOrEmpty()) {
			data.chatShortcuts = mutableListOf(ChatShortcutData(command, keyName))
		}
		save()
	}

	fun chatShortcuts(): List<ChatShortcutData> {
		data.sanitize()
		return data.chatShortcuts!!
	}

	fun mutableChatShortcuts(): MutableList<ChatShortcutData> {
		data.sanitize()
		return data.chatShortcuts!!
	}

	fun updateEntry(key: String, updater: (EntryData) -> Unit) {
		val entry = entries.getOrPut(key) { EntryData() }
		updater(entry)
		save()
	}

	fun column(key: String): ColumnData? = columns[key]

	fun migrateColumn(oldKey: String, newKey: String) {
		val oldColumn = columns.remove(oldKey) ?: return
		columns.putIfAbsent(newKey, oldColumn)
		save()
	}

	fun updateColumn(key: String, updater: (ColumnData) -> Unit) {
		val column = columns.getOrPut(key) { ColumnData() }
		updater(column)
		save()
	}

	fun isAutoUpdateEnabled(): Boolean = data.autoUpdateEnabled ?: true

	fun setAutoUpdateEnabled(enabled: Boolean) {
		data.autoUpdateEnabled = enabled
		save()
	}

	fun isAutoUpdateNoticeShown(): Boolean = data.autoUpdateNoticeShown ?: false

	fun markAutoUpdateNoticeShown() {
		data.autoUpdateNoticeShown = true
		save()
	}

	class ConfigData {
		var entries: MutableMap<String, EntryData>? = linkedMapOf()
		var columns: MutableMap<String, ColumnData>? = linkedMapOf()
		var chatShortcuts: MutableList<ChatShortcutData>? = mutableListOf()
		var autoUpdateEnabled: Boolean? = true
		var autoUpdateNoticeShown: Boolean? = false

		fun sanitize() {
			if (entries == null) {
				entries = linkedMapOf()
			}
			if (columns == null) {
				columns = linkedMapOf()
			}
			if (chatShortcuts == null) {
				chatShortcuts = mutableListOf()
			}
			if (autoUpdateEnabled == null) autoUpdateEnabled = true
			if (autoUpdateNoticeShown == null) autoUpdateNoticeShown = false
		}
	}

	class ChatShortcutData(
		var message: String = "",
		var keyName: String? = null,
	)

	class EntryData {
		var enabled: Boolean? = null
		var settingsExpanded: Boolean? = null
		var switchValue: Boolean? = null
		var value: String? = null
		var keyName: String? = null
		var selected: Int? = null
		var sliderPercentage: Float? = null
		var color: Int? = null
	}

	class ColumnData {
		var x: Float? = null
		var y: Float? = null
		var extended: Boolean? = null
	}
}
