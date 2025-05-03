package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.fabricmc.api.ModInitializer
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.TitleScreen
import org.aresclient.ares.api.Plugin
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.*
import org.aresclient.ares.api.gui.AresScreen
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.settings.BindSetting
import org.aresclient.ares.impl.AresPlugin
import org.aresclient.ares.impl.util.ChatUtil
import org.aresclient.ares.impl.util.FriendUtil
import org.slf4j.LoggerFactory
import java.io.File

class Ares: ModInitializer, Wrapper {
	companion object {
		val LOGGER = LoggerFactory.getLogger("Ares")
		@JvmStatic val EVENT_MANAGER = AresEventManager()

		val PLUGINS = ArrayList<Plugin>()

		val SETTINGS_FILE = File("ares/config/settings.json")
		val SETTINGS = MapSetting().also {
			try {
				it.read(SETTINGS_FILE)
			} catch (_: Exception) {
			}
		}

		val COMMAND_PREFIX = SETTINGS.addString("CmdPrefix", "-")

		fun load(plugin: Plugin) {
			val start = System.currentTimeMillis()

			plugin.globals.forEach(Instrument::registerEvents)
			plugin.modules.forEach(Instrument::registerEvents)

			plugin.init()
			PLUGINS.add(plugin)

			LOGGER.info(
				"Loaded plugin {} with {} globals, {} modules and {} commands in {} milliseconds.",
				plugin.name, plugin.globals.size, plugin.modules.size, plugin.commands.size,
				System.currentTimeMillis() - start
			)
		}
	}

	@field:EventHandler
	val tickEventListener = EventListener<TickEvent> { event ->
		if(event.era != Era.BEFORE) return@EventListener
		else if(event is TickEvent.Client) PLUGINS.forEach { plugin ->
			plugin.tickClient()
		}
		else if(event is TickEvent.Motion)PLUGINS.forEach { plugin ->
			plugin.tickMotion()
		}
	}

	@field:EventHandler
	val renderEventListener = EventListener<RenderEvent> { event ->
		if(event is RenderEvent.Hud) {
			val state = Renderer.begin2d()
			PLUGINS.forEach { plugin ->
				plugin.renderHud(event.tickDelta, state)
			}
			Renderer.end(state)
		}
		else if(event is RenderEvent.World) {
			val state = Renderer.begin3d()
			PLUGINS.forEach { plugin ->
				plugin.renderWorld(event.tickDelta, state)
			}
			Renderer.end(state)
		}
	}

	@field:EventHandler
	val inputEventListener = EventListener<InputEvent> { event ->
		if(MC.currentScreen !is TitleScreen && MC.currentScreen !is AresScreen && MC.currentScreen != null)
			return@EventListener

		when(event) {
			is InputEvent.Keyboard.Pressed  -> BindSetting.getAll().forEach { it.triggerCallback(event.key, true, event.repeat) }
			is InputEvent.Keyboard.Released -> BindSetting.getAll().forEach { it.triggerCallback(event.key, false, false) }
			is InputEvent.Mouse.Pressed     -> BindSetting.getAll().forEach { it.triggerCallback(event.key, true, event.repeat) }
			is InputEvent.Mouse.Released    -> BindSetting.getAll().forEach { it.triggerCallback(event.key, false, false) }
		}
	}

	private fun BindSetting.triggerCallback(key: Int, state: Boolean, repeat: Boolean) {
		if(this.value != key) return
		this.callback.accept(state, repeat)
	}

	@field:EventHandler
	val chatListener = EventListener<ChatEvent> { event ->
		if(event.message.startsWith(COMMAND_PREFIX.value)) {
			Command.execute(ChatUtil, event.message.substring(COMMAND_PREFIX.value.length))
			MC.inGameHud.chatHud.addToMessageHistory(event.message)
			event.isCancelled = true
		}
	}

	@field:EventHandler
	val charTypedListener = EventListener<CharTypedEvent> { event ->
		if(MC.currentScreen == null && !MC.NULL && COMMAND_PREFIX.value.length == 1 && event.codePoint.toChar() == COMMAND_PREFIX.value[0])
			MC.setScreen(ChatScreen(""))
	}

	@field:EventHandler
	val shutdownListener = EventListener<ShutdownEvent> {
		FriendUtil.save()
		SETTINGS_FILE.parentFile.mkdirs()
		SETTINGS.write(SETTINGS_FILE)
		Renderer.cleanup()
	}

	override fun onInitialize() {
		val start = System.currentTimeMillis()

		EVENT_MANAGER.register(this)

		FriendUtil
		load(AresPlugin)
		// TODO: Dynamic Plugin Loading?

		LOGGER.info("Ares loaded {} plugins in {} milliseconds.", PLUGINS.size, System.currentTimeMillis() - start)
	}
}

