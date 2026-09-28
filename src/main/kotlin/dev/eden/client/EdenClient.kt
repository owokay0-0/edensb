package dev.eden.client

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.blaze3d.platform.InputConstants
import dev.eden.client.feature.DungeonBreakerFeature
import dev.eden.client.feature.EtherwarpFeature
import dev.eden.client.feature.EdenFeatures
import dev.eden.client.feature.GoldenFishCiFeature
import dev.eden.client.feature.PerformanceMetricsFeature
import dev.eden.client.feature.SkyblockDataTracker
import dev.eden.client.feature.SkyblockHudRenderer
import dev.eden.client.feature.SkyblockTooltipFeatures
import dev.eden.client.feature.ShitterAlertFeature
import dev.eden.client.feature.TeammateHighlightRenderer
import dev.eden.client.feature.ZoomFeature
import dev.eden.client.render.EdenNvgPipRenderer
import dev.eden.client.update.UpdateManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.slf4j.LoggerFactory

class EdenClient : ClientModInitializer {
	override fun onInitializeClient() {
		EdenConfig.load()
		EdenConfig.migrateEntryPrefixes(
			"Misc.Etherwarp" to "Dungeons.Etherwarp",
			"QoL.Player Hider" to "Visuals.Player Hider",
			"QoL.Player Size" to "Visuals.Player Size",
			"QoL.Hit Color" to "Visuals.Hit Color",
			"QoL.Fullbright" to "Visuals.Fullbright",
			"QoL.Performance HUD" to "Visuals.Performance HUD",
			"QoL.Render Optimizer" to "Visuals.Render Optimizer",
			"QoL.Custom Name Replacer" to "Visuals.Name Replace",
			"QoL.Zoom" to "Visuals.Zoom",
			"SkyBlock.Pet Overlay" to "Visuals.Pet Overlay",
			"SkyBlock.Pressure Display" to "Visuals.Pressure Display",
			"SkyBlock.Low HP Indicator" to "Visuals.Low HP Indicator",
			"SkyBlock.Drill Fuel Meter" to "Visuals.Drill Fuel Meter",
			"SkyBlock.Action Bar Cleanup" to "Visuals.Action Bar Cleanup",
			"QoL.Diana QoL" to "Misc.Diana QoL",
			"QoL.Golden Fish CI" to "Misc.Golden Fish CI",
			"QoL.Infinite Chat" to "Misc.Infinite Chat",
			"SkyBlock.Missing Enchants" to "Misc.Missing Enchants",
			"SkyBlock.Compact Pet Level" to "Misc.Compact Pet Level",
		)
		EdenConfig.removeEntryPrefix("SkyBlock.Only in SkyBlock")
		EdenConfig.removeEntryPrefix("Visuals.Name Replace.Original Name")
		EdenConfig.removeEntryPrefix("QoL.Day Reminder")
		EdenConfig.removeEntryPrefix("QoL.Reminder")
		EdenConfig.removeEntryPrefix("Misc.Reminder")
		EdenConfig.removeEntryPrefix("QoL.Leap Frog")
		EdenConfig.removeEntryPrefix("Misc.Leap Frog")
		EdenConfig.removeEntryPrefix("QoL.Smart Term AC")
		EdenConfig.removeEntryPrefix("Misc.Smart Term AC")
		EdenConfig.removeEntryPrefix("Dungeons.Smart Term AC")
		EdenConfig.removeEntryPrefix("Visuals.Pet Overlay.XP ETA")
		EdenConfig.migrateColumn("SkyBlock", "Visuals")
		EdenConfig.migrateColumn("QoL", "Misc")
		EdenConfig.migrateEtherwarpLeftClickMode()
		EdenConfig.migrateZoomIntensity()
		EdenConfig.migrateChatShortcuts()
		EdenFeatures.loadFromConfig()
		val updateManager = UpdateManager.create(LoggerFactory.getLogger(MOD_ID))
		updateManager.start()

		PictureInPictureRendererRegistry.register { context ->
			EdenNvgPipRenderer()
		}
		DungeonBreakerFeature.register()
		EtherwarpFeature.register()
		TeammateHighlightRenderer.register()
		SkyblockDataTracker.register()
		SkyblockHudRenderer.register()
		SkyblockTooltipFeatures.register()
		GoldenFishCiFeature.register()
		PerformanceMetricsFeature.register()
		ShitterAlertFeature.register()
		ZoomFeature.register()

		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				LiteralArgumentBuilder.literal<FabricClientCommandSource>("eden")
					.executes { context ->
						context.source.client.execute {
							openMenu(context.source.client)
						}
						1
					}
					.then(
						LiteralArgumentBuilder.literal<FabricClientCommandSource>("hud").executes { context ->
							context.source.client.execute {
								context.source.client.gui.setScreen(EdenHudEditorScreen(null))
							}
							1
						},
					)
					.then(autoCommand(updateManager)),
			)
		}

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			val menuKeyDown = InputConstants.isKeyDown(InputConstants.KEY_RSHIFT)
			if (menuKeyDown && !wasMenuKeyDown) {
				toggleMenu(client)
			}
			wasMenuKeyDown = menuKeyDown
			EdenKeybinds.tick(client)
			showAutoUpdateNotice(client)
		}
	}

	companion object {
		const val MOD_ID: String = "eden"

		private var wasMenuKeyDown: Boolean = false
		private var noticeDelayTicks: Int = 3
		private const val AUTO_UPDATE_NOTICE_COLOR: Int = 0xFFAA01

		fun openMenu(client: Minecraft = Minecraft.getInstance()) {
			EdenMoulConfig.open()
		}

		private fun autoCommand(manager: UpdateManager): LiteralArgumentBuilder<FabricClientCommandSource> {
			return LiteralArgumentBuilder.literal<FabricClientCommandSource>("auto")
				.executes { context ->
					context.source.client.player?.sendSystemMessage(
						Component.literal("[Eden] auto updates is ${if (manager.isEnabled) "on" else "off"}"),
					)
					1
				}
				.then(LiteralArgumentBuilder.literal<FabricClientCommandSource>("on").executes { context ->
					manager.setEnabled(true)
					context.source.client.player?.sendSystemMessage(
						Component.literal("[Eden] auto updates is on")
							.withStyle { style -> style.withColor(AUTO_UPDATE_NOTICE_COLOR) },
					)
					1
				})
				.then(LiteralArgumentBuilder.literal<FabricClientCommandSource>("off").executes { context ->
					manager.setEnabled(false)
					context.source.client.player?.sendSystemMessage(Component.literal("[Eden] auto updates is off"))
					1
				})
		}

		private fun showAutoUpdateNotice(client: Minecraft) {
			if (!EdenConfig.isAutoUpdateEnabled() || EdenConfig.isAutoUpdateNoticeShown() || client.player == null) return
			if (noticeDelayTicks-- > 0) return
			EdenConfig.markAutoUpdateNoticeShown()
			val message = Component.literal("[Eden] auto updates is on, click ")
				.withStyle { style -> style.withColor(AUTO_UPDATE_NOTICE_COLOR) }
				.append(Component.literal("HERE").withStyle { style ->
					style.withColor(AUTO_UPDATE_NOTICE_COLOR)
						.withUnderlined(true)
						.withClickEvent(ClickEvent.RunCommand("/eden auto off"))
						.withHoverEvent(HoverEvent.ShowText(Component.literal("Turn automatic updates off")))
				})
				.append(
					Component.literal(" to turn off")
						.withStyle { style -> style.withColor(AUTO_UPDATE_NOTICE_COLOR) },
				)
			client.player?.sendSystemMessage(message)
		}

		private fun toggleMenu(client: Minecraft) {
			if (client.gui.screen() == null) {
				openMenu(client)
			}
		}
	}
}
