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
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.JsonSettingSerializer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.Ares
import org.slf4j.LoggerFactory
import java.io.File

class Main: ModInitializer {

	companion object {
		val MOD_ID = "ares-main"
		val LOGGER = LoggerFactory.getLogger(MOD_ID)

		val PLUGINS = ArrayList<Plugin>()

		val SETTINGS_FILE = File("/ares/config/settings.json")
		val SETTINGS_SERIALIZER:JsonSettingSerializer = JsonSettingSerializer { prettyPrint = true }
		val SETTINGS:Setting.Map<JsonElement> = SETTINGS_SERIALIZER.read(SETTINGS_FILE)
		val PLUGIN_SETTINGS:Setting.Map<*> = SETTINGS.addMap("Plugins")

		@JvmStatic val EVENTS = AresEventManager()
		@JvmStatic val MC:MinecraftClient = MinecraftClient.getInstance()

		fun tickClient() = PLUGINS.forEach {
			it.modules.forEach(Instrument::tick)
			it.globals.forEach(Instrument::tick)
		}

		fun tickMotion() = PLUGINS.forEach { it.modules.forEach(Module::motion) }

		fun renderHud(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) = PLUGINS.forEach {
			it.modules.forEach { module -> module.renderHud(delta, buffers, matrixStack) }
		}

		fun renderWorld(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) = PLUGINS.forEach {
			it.modules.forEach { module -> module.renderWorld(delta, buffers, matrixStack) }
		}

		@EventHandler
		val tickEventListener = EventListener<TickEvent> {
			if (it.era != Era.BEFORE) return@EventListener
			else if (it is TickEvent.Client) tickClient()
			else if (it is TickEvent.Motion) tickMotion()
		}

		@EventHandler
		val renderEventListener = EventListener<RenderEvent> {
			if (it is RenderEvent.Hud) {
				val state = Renderer.begin2d()
				renderHud(it.renderTickCounter.fixedDeltaTicks, state.buffers, state.matrixStack)
				Renderer.end(state)
			}
			else if (it is RenderEvent.World) {
				val state = Renderer.begin2d()
				renderWorld(it.tickDelta, state.buffers, state.matrixStack)
				Renderer.end(state)
			}
		}

		@EventHandler
		val inputEventListener = EventListener<InputEvent> {
			val p:Pair<Int, Boolean> = when (it) {
				is InputEvent.Keyboard.Pressed  -> Pair(it.key, true)
				is InputEvent.Keyboard.Released -> Pair(it.key, false)
				is InputEvent.Mouse.Pressed     -> Pair(it.key, true)
				is InputEvent.Mouse.Released    -> Pair(it.key, false)
				else                            -> return@EventListener
			}

			Setting.Bind.getAll().forEach { bind ->
					if (bind.value != p.first) return@forEach
					bind.callback.accept(p.second)
			}
		}

		@EventHandler
		val shutdownListener = EventListener<ShutdownEvent> {
			SETTINGS_FILE.mkdirs()
			SETTINGS_SERIALIZER.write(SETTINGS, SETTINGS_FILE)
			Renderer.cleanup()
		}

		init {
			EVENTS.register(this.javaClass)
		}
	}

	override fun onInitialize() {
		LOGGER.info("Hello Fabric World!")

		Ares
		// TODO: Dynamic Plugin Loading?
	}

}

