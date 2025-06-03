package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.fabricmc.api.ModInitializer
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.aresclient.ares.api.Plugin
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.*
import org.aresclient.ares.api.gui.AresScreen
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.drawer.HudDrawer
import org.aresclient.ares.api.nrender.drawer.WorldDrawer
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
		private const val MOD_ID = "ares"
		private const val MOD_NAME = "Ares"
		private val LOGGER = LoggerFactory.getLogger(MOD_NAME)
		private val EVENT_MANAGER = AresEventManager()
		private val PLUGINS = ArrayList<Plugin>()
		private val SETTINGS_FILE = File("ares/config/settings.json")
		private val SETTINGS = MapSetting().also {
			try {
				it.read(SETTINGS_FILE)
			} catch (_: Exception) {
			}
		}
		private val COMMAND_PREFIX_SETTING = SETTINGS.addString("CmdPrefix", "-")
		private val HUD_DRAWER = HudDrawer()
		private val HUD_MATRIX_STACK = MatrixStack()
		private val WORLD_DRAWER = WorldDrawer()

		@JvmStatic fun getModId() = MOD_ID
		@JvmStatic fun getModName() = MOD_NAME
		@JvmStatic fun getLogger() = LOGGER
		@JvmStatic fun getEventManager() = EVENT_MANAGER
		@JvmStatic fun getPlugins() = PLUGINS
		@JvmStatic fun getSettingsFile() = SETTINGS_FILE
		@JvmStatic fun getSettings() = SETTINGS
		@JvmStatic fun getCommandPrefixSetting() = COMMAND_PREFIX_SETTING
		@JvmStatic fun getHudDrawer() = HUD_DRAWER
		@JvmStatic fun getWorldDrawer() = WORLD_DRAWER

		@JvmStatic
		fun identifier(path: String): Identifier = Identifier.of(MOD_ID, path)
	}

	@field:EventHandler
	private val tickEventListener = EventListener<TickEvent> { event ->
		if(event.era != Era.BEFORE) return@EventListener
		else if(event is TickEvent.Client) PLUGINS.forEach(Plugin::tickClient)
		else if(event is TickEvent.Motion) PLUGINS.forEach(Plugin::tickMotion)
	}

	@field:EventHandler
	private val renderEventListener = EventListener<RenderEvent> { event ->
		if(event is RenderEvent.Hud) {
			HUD_DRAWER.begin()
			PLUGINS.forEach { plugin ->
				plugin.renderHud(HUD_DRAWER, HUD_MATRIX_STACK, event.tickDelta)
			}
			HUD_DRAWER.draw()
		} else if(event is RenderEvent.World) {
			WORLD_DRAWER.begin()
			PLUGINS.forEach { plugin ->
				plugin.renderWorld(WORLD_DRAWER, event.tickDelta)
			}
			WORLD_DRAWER.draw()
		}
	}

	@field:EventHandler
	private val inputEventListener = EventListener<InputEvent> { event ->
		if(event.textboxFocused || MC.currentScreen !is TitleScreen && MC.currentScreen !is AresScreen && MC.currentScreen != null)
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
	private val chatListener = EventListener<ChatEvent> { event ->
		if(event.message.startsWith(COMMAND_PREFIX_SETTING.value)) {
			Command.execute(ChatUtil, event.message.substring(COMMAND_PREFIX_SETTING.value.length))
			MC.inGameHud.chatHud.addToMessageHistory(event.message)
			event.isCancelled = true
		}
	}

	@field:EventHandler
	private val charTypedListener = EventListener<CharTypedEvent> { event ->
		if(MC.currentScreen == null && !MC.NULL && COMMAND_PREFIX_SETTING.value.length == 1 && event.codePoint.toChar() == COMMAND_PREFIX_SETTING.value[0])
			MC.setScreen(ChatScreen(""))
	}

	@field:EventHandler
	private val shutdownListener = EventListener<ShutdownEvent> {
		FriendUtil.save()
		SETTINGS_FILE.parentFile.mkdirs()
		SETTINGS.write(SETTINGS_FILE)
		Renderer.cleanup()
	}

	override fun onInitialize() {
		val start = System.currentTimeMillis()

		EVENT_MANAGER.register(this)

		FriendUtil // TODO: make a global variable

		loadPlugin(AresPlugin)
		// TODO: Dynamic Plugin Loading

		LOGGER.info("Ares loaded {} plugins in {} milliseconds.", PLUGINS.size, System.currentTimeMillis() - start)
	}

	private fun loadPlugin(plugin: Plugin) {
		val start = System.currentTimeMillis()

		plugin.globals.forEach(Instrument::registerEvents)
		plugin.modules.forEach(Instrument::registerEvents)
		plugin.modules.filter(Module::isEnabled).forEach(Module::onEnable)

		plugin.init()
		PLUGINS.add(plugin)

		Companion.LOGGER.info(
			"Loaded plugin {} with {} globals, {} modules and {} commands in {} milliseconds.",
			plugin.name, plugin.globals.size, plugin.modules.size, plugin.commands.size,
			System.currentTimeMillis() - start
		)
	}
}
