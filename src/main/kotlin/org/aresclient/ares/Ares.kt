package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen
import org.aresclient.ares.api.Plugin
import org.aresclient.ares.api.events.AresEventManager
import org.aresclient.ares.api.events.Era
import org.aresclient.ares.api.events.InputEvent
import org.aresclient.ares.api.events.RenderEvent
import org.aresclient.ares.api.events.ShutdownEvent
import org.aresclient.ares.api.events.TickEvent
import org.aresclient.ares.api.gui.AresScreen
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.SettingGroup
import org.aresclient.ares.api.setting.settings.BindSetting
import org.aresclient.ares.impl.AresPlugin
import org.slf4j.LoggerFactory
import java.io.File

class Ares: ModInitializer {
	companion object {
		val LOGGER = LoggerFactory.getLogger("Ares")
		val PLUGINS = ArrayList<Plugin>()

		val SETTINGS_FILE = File("ares/config/settings.json")
		val SETTINGS = SettingGroup().also {
			try {
				it.read(SETTINGS_FILE)
			} catch (_: Exception) {
			}
		}

		@JvmStatic val MC: MinecraftClient = MinecraftClient.getInstance()
		@JvmStatic val EVENT_MANAGER = AresEventManager()

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
				plugin.renderHud(event.tickDelta, state.buffers, state.matrixStack)
			}
			Renderer.end(state)
		}
		else if(event is RenderEvent.World) {
			val state = Renderer.begin3d(event.matrix4f)
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
	val shutdownListener = EventListener<ShutdownEvent> {
		SETTINGS_FILE.parentFile.mkdirs()
		SETTINGS.write(SETTINGS_FILE)
		Renderer.cleanup()
	}

	override fun onInitialize() {
		val start = System.currentTimeMillis()

		EVENT_MANAGER.register(this)

		load(AresPlugin)
		// TODO: Dynamic Plugin Loading?

		LOGGER.info("Ares loaded {} plugins in {} milliseconds.", PLUGINS.size, System.currentTimeMillis() - start)
	}
}

