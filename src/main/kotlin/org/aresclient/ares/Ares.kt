package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import kotlinx.serialization.json.JsonElement
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import org.aresclient.ares.api.Plugin
import org.aresclient.ares.api.events.AresEventManager
import org.aresclient.ares.api.events.Era
import org.aresclient.ares.api.events.InputEvent
import org.aresclient.ares.api.events.RenderEvent
import org.aresclient.ares.api.events.ShutdownEvent
import org.aresclient.ares.api.events.TickEvent
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.AresPlugin
import org.aresclient.ares.impl.JsonSettingSerializer
import org.slf4j.LoggerFactory
import java.io.File

class Ares: ModInitializer {
	companion object {
		val LOGGER = LoggerFactory.getLogger("Ares")
		val PLUGINS = ArrayList<Plugin>()

		val SETTINGS_FILE = File("ares/config/settings.json")
		val SETTINGS_SERIALIZER: JsonSettingSerializer = JsonSettingSerializer { prettyPrint = true }
		val SETTINGS: Setting.Map<JsonElement> = SETTINGS_SERIALIZER.read(SETTINGS_FILE)

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
			val state = Renderer.begin3d()
			PLUGINS.forEach { plugin ->
				plugin.renderWorld(event.tickDelta, state.buffers, state.matrixStack)
			}
			Renderer.end(state)
		}
	}

	@field:EventHandler
	val inputEventListener = EventListener<InputEvent> { event ->
		val p: Pair<Int, Boolean> = when(event) {
			is InputEvent.Keyboard.Pressed  -> Pair(event.key, true)
			is InputEvent.Keyboard.Released -> Pair(event.key, false)
			is InputEvent.Mouse.Pressed     -> Pair(event.key, true)
			is InputEvent.Mouse.Released    -> Pair(event.key, false)
			else                            -> return@EventListener
		}

		Setting.Bind.getAll().forEach { bind ->
			if(bind.value != p.first) return@forEach
			bind.callback.accept(p.second)
		}
	}

	@field:EventHandler
	val shutdownListener = EventListener<ShutdownEvent> {
		SETTINGS_FILE.mkdirs()
		SETTINGS_SERIALIZER.write(SETTINGS, SETTINGS_FILE)
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

