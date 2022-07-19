package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.Mesh
import net.meshmc.mesh.api.packet.client.CPacketChatMessage
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.client.InputEvent
import net.meshmc.mesh.event.events.client.PacketEvent
import net.meshmc.mesh.event.events.client.ScreenOpenedEvent
import net.meshmc.mesh.event.events.client.TickEvent
import net.meshmc.mesh.event.events.render.RenderEvent
import org.apache.logging.log4j.LogManager
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
class Ares: Mesh.Initializer {
    companion object {
        val MESH = Mesh.getMesh()
        val LOGGER = LogManager.getLogger("Ares")

        var SETTINGS_FILE = File("ares/configs/settings.json")
        val SETTINGS = Settings.read(SETTINGS_FILE) {
           prettyPrint = true
        }

        val GLOBALS = arrayListOf<Global>()
        val MODULES = arrayListOf<Module>()

        val TITLE_SETTING = SETTINGS.boolean("title", true)
        private val TITLE_SCREEN by lazy { AresTitleScreen() }
        private val GAME_SCREEN by lazy { AresGameScreen() }

        @field:EventHandler
        private val screenOpenedEventListener = EventListener<ScreenOpenedEvent> { event ->
            // TODO: call delete() on screen after close
            if(TITLE_SETTING.value && event.isMainMenu) MESH.minecraft.openScreen(TITLE_SCREEN.getScreen())
        }

        @field:EventHandler
        private val tickEventListener = EventListener<TickEvent.Client> { event ->
            if(event.era == MeshEvent.Era.BEFORE) {
                MODULES.forEach(Module::tick)
                GLOBALS.forEach(Global::tick)
            }
        }

        @field:EventHandler
        private val renderEventListener = EventListener<RenderEvent.Hud> { event ->
            MODULES.forEach { it.renderHud(event.tickDelta) }
        }

        @field:EventHandler
        private val motionEventListener = EventListener<TickEvent.Motion> { event ->
            if(event.era == MeshEvent.Era.BEFORE) MODULES.forEach(Module::motion)
        }

        @field:EventHandler
        private val onChatMessageSent = EventListener<PacketEvent.Sent> { event ->
            if(event.era == MeshEvent.Era.BEFORE && event.packet is CPacketChatMessage) {
                val packet = event.packet as CPacketChatMessage
                if(packet.message.startsWith(Command.prefix)) {
                    Command.processCommand(packet.message)
                    event.isCancelled = true
                }
            }
        }

        @field:EventHandler
        private val onInputKey = EventListener<InputEvent.Keyboard> { event ->
            if(AresGameScreen.BIND.value == event.key && event.state == InputEvent.Keyboard.State.PRESSED)
                MESH.minecraft.openScreen(GAME_SCREEN.getScreen())

            for(module in MODULES) {
                if(module.getBind() == event.key) {
                    when(event.state) {
                        InputEvent.Keyboard.State.PRESSED -> {
                            if(!module.pressed) {
                                if(module.getToggleState() == Module.TogglesWhen.PRESSED)
                                    module.toggle()
                                if(module.getToggleState() == Module.TogglesWhen.HELD_DOWN) {
                                    module.enable()
                                }

                                module.pressed = true
                            }
                        }
                        InputEvent.Keyboard.State.RELEASED -> {
                            if(module.getToggleState() == Module.TogglesWhen.RELEASED) {
                                module.toggle()
                            }
                            if(module.getToggleState() == Module.TogglesWhen.HELD_DOWN) {
                                module.disable()
                            }

                            module.pressed = false
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun init() {
        val start = System.currentTimeMillis()

        // register companion object for basic module / manager events
        MESH.eventManager.register(Ares::class.java)

        // load globals into classpath
        RenderGlobal
        RotationGlobal
        GLOBALS.forEach { MESH.eventManager.register(it.javaClass) }

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
            if(!File("ares").exists()) File("ares").mkdir()
            if(!File("ares/configs").exists()) File("ares/configs").mkdir()
            SETTINGS.write(SETTINGS_FILE)
        })

        LOGGER.info("Ares loaded {} modules and {} globals in {} milliseconds", MODULES.size, GLOBALS.size, System.currentTimeMillis() - start)
    }
}
