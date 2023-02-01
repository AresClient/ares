package org.aresclient.ares

import dev.tigr.simpleevents.EventManager
import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.loader.Mod
import net.meshmc.mesh.loader.Mod.Instance
import net.meshmc.mesh.loader.Mod.Interface
import org.apache.logging.log4j.LogManager
import org.aresclient.ares.api.ICreator
import org.aresclient.ares.api.IMinecraft
import org.aresclient.ares.api.packet.ChatMessageC2SPacket
import org.aresclient.ares.command.*
import org.aresclient.ares.global.Global
import org.aresclient.ares.global.RenderGlobal
import org.aresclient.ares.global.RotationGlobal
import org.aresclient.ares.gui.impl.game.AresGameScreen
import org.aresclient.ares.gui.impl.title.AresTitleScreen
import org.aresclient.ares.module.Module
import org.aresclient.ares.module.player.AntiAFK
import org.aresclient.ares.module.render.ESP
import org.aresclient.ares.module.render.TestModule
import java.io.File

/*
 * Ares 3.x
 * third rewrite - 3x better
 * developed by Tigermouthbear & Makrennel
 */
class Ares: Mod.Initializer {
    companion object {
        @Instance
        lateinit var INSTANCE: Ares

        val LOGGER = LogManager.getLogger("Ares")
        val EVENT_MANAGER = EventManager()

        var SETTINGS_FILE = File("assets.ares/configs/settings.json")
        val SETTINGS = Settings.read(SETTINGS_FILE) {
            prettyPrint = true
        }

        val GLOBALS = arrayListOf<Global>()
        val MODULES = arrayListOf<Module>()

        val TITLE_SETTING = SETTINGS.boolean("Title", true)
        private val TITLE_SCREEN by lazy { AresTitleScreen() }
        private val GAME_SCREEN by lazy { AresGameScreen() }

        fun clientTick() {
            MODULES.forEach(Module::tick)
            GLOBALS.forEach(Global::tick)
        }

        fun motionTick() {
            MODULES.forEach(Module::motion)
        }

        fun renderHud(tickDelta: Float) {
            MODULES.forEach { it.renderHud(tickDelta) }
        }
    }

    @Interface
    lateinit var creator: ICreator
    @Interface
    lateinit var minecraft: IMinecraft

    @field:EventHandler
    private val screenOpenedEventListener = EventListener<ScreenOpenedEvent> { event ->
        // TODO: call delete() on screen after close
        if (TITLE_SETTING.value && event.mainMenu) INSTANCE.minecraft.openScreen(TITLE_SCREEN.getScreen())
    }

    @field:EventHandler
    private val onChatMessageSent = EventListener<PacketEvent.Sent> { event ->
        if(event.era == PacketEvent.Era.BEFORE && event.packet is ChatMessageC2SPacket) {
            val packet = event.packet
            if(packet.message.startsWith(Command.prefix)) {
                Command.processCommand(packet.message)
                event.isCancelled = true
            }
        }
    }

    // TODO: MAYBE MOVE THIS TO THE SETTINGS CLASS?
    @field:EventHandler
    private val onInputEvent = EventListener<InputEvent> { event ->
        var key: Int? = null
        var state: Boolean? = null

        when(event) {
            is InputEvent.Keyboard -> {
                key = event.key
                state = event.state == InputEvent.Keyboard.State.PRESSED
            }
            is InputEvent.Mouse.Pressed -> {
                key = event.key
                state = true
            }
            is InputEvent.Mouse.Released -> {
                key = event.key
                state = false
            }
        }

        if(key == null || state == null) return@EventListener

        if(AresGameScreen.BIND.value == key && state)
            INSTANCE.minecraft.openScreen(GAME_SCREEN.getScreen())

        for(module in MODULES) if(module.getBind() == key) {
            if(state) {
                if(!module.pressed) {
                    if(module.getToggleState() == Module.TogglesWhen.PRESS)
                        module.toggle()
                    if(module.getToggleState() == Module.TogglesWhen.HOLD) {
                        module.enable()
                    }

                    module.pressed = true
                }
            } else {
                if(module.getToggleState() == Module.TogglesWhen.RELEASE) {
                    module.toggle()
                }
                if(module.getToggleState() == Module.TogglesWhen.HOLD) {
                    module.disable()
                }

                module.pressed = false
            }
        }
    }

    override fun init(mod: Mod?) {
        val start = System.currentTimeMillis()

        // register instance for basic module / manager events
        EVENT_MANAGER.register(INSTANCE)

        // load globals into classpath
        RenderGlobal
        RotationGlobal
        GLOBALS.forEach { EVENT_MANAGER.register(it.javaClass) }

        // load modules into classpath
        AntiAFK

        ESP
        TestModule

        // load commands into classpath
        BindCommand
        ConfigCommand
        SettingCommand
        UnbindCommand

        // save settings on shutdown
        // TODO: THIS DOESNT WORK EVERY TIME
        Runtime.getRuntime().addShutdownHook(Thread {
            /*Buffer.clear() // TODO: DO WE NEED THIS?
            Shader.clear()
            Texture.clear()
            BlurFrameBuffer.clear()
            MSAAFrameBuffer.clear()
            SkyBox.clear()*/
            if(!File("assets.ares").exists()) File("assets.ares").mkdir()
            if(!File("assets.ares/configs").exists()) File("assets.ares/configs").mkdir()
            SETTINGS.write(SETTINGS_FILE)
        })

        LOGGER.info("Ares loaded {} modules and {} globals in {} milliseconds", MODULES.size, GLOBALS.size, System.currentTimeMillis() - start)
    }
}
