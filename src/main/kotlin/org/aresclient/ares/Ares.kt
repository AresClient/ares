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
import org.aresclient.ares.command.BindCommand
import org.aresclient.ares.command.ConfigCommand
import org.aresclient.ares.command.SettingCommand
import org.aresclient.ares.command.UnbindCommand
import org.aresclient.ares.gui.AresTitleScreen
import org.aresclient.ares.manager.RotationManager
import org.aresclient.ares.module.Module
import org.aresclient.ares.module.render.*
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

        val MANAGERS = arrayListOf<Manager>()
        val MODULES = arrayListOf<Module>()

        @field:EventHandler
        private val screenOpenedEventListener = EventListener<ScreenOpenedEvent> { event ->
            if(event.isMainMenu) MESH.minecraft.openScreen(AresTitleScreen())
        }

        @field:EventHandler
        private val tickEventListener = EventListener<TickEvent.Client> { event ->
            if(event.era == MeshEvent.Era.BEFORE) {
                for(module in MODULES) if(module.isEnabled()) module.tick()
                MANAGERS.forEach(Manager::tick)
            }
        }

        @field:EventHandler
        private val renderEventListener = EventListener<RenderEvent> { event ->
            when(event.type) {
                RenderEvent.Type.HUD -> for(module in MODULES) if(module.isEnabled()) module.renderHud()
                RenderEvent.Type.WORLD -> for(module in MODULES) if(module.isEnabled()) module.renderWorld()
                else -> Unit
            }
        }

        @field:EventHandler
        private val motionEventListener = EventListener<TickEvent.Motion> { event ->
            if(event.era == MeshEvent.Era.BEFORE)
                for(module in MODULES) if(module.isEnabled()) module.motion()
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

        // load managers into classpath
        RotationManager()

        // load modules into classpath
        ESP
        TestModule

        // load commands into classpath
        BindCommand
        ConfigCommand
        SettingCommand
        UnbindCommand

        // register events after loading modules / managers
        MODULES.forEach(Module::postInit)
        for(manager in MANAGERS) MESH.eventManager.register(manager)

        // save settings on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            /*Buffer.clear() // TODO: DO WE NEED THIS?
            Shader.clear()
            Texture.clear()
            BlurFrameBuffer.clear()
            MSAAFrameBuffer.clear()
            SkyBox.clear()*/
            if(!File("ares/configs").exists()) File("ares/configs").mkdir()
            SETTINGS.write(SETTINGS_FILE)
        })

        LOGGER.info("Ares loaded {} modules and {} managers in {} milliseconds", MODULES.size, MANAGERS.size, System.currentTimeMillis() - start)
    }
}
