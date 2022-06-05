package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.Mesh
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.client.TickEvent
import net.meshmc.mesh.event.events.render.RenderEvent
import org.apache.logging.log4j.LogManager
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

        private val SETTINGS_FILE = File("Ares/settings.json")
        val SETTINGS = Settings.read(SETTINGS_FILE) {
           prettyPrint = true
        }

        val MANAGERS = arrayListOf<Manager>()
        val MODULES = arrayListOf<Module>()

        @field:EventHandler
        private val tickEventListener = EventListener<TickEvent.Client> { event ->
            if(event.era == MeshEvent.Era.BEFORE) {
                for(module in MODULES) if(module.isEnabled()) module.tick()
                MANAGERS.forEach(Manager::tick)
            }
        }

        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        @field:EventHandler
        private val renderEventListener = EventListener<RenderEvent> { event ->
            when(event.type) {
                RenderEvent.Type.HUD -> for(module in MODULES) if(module.isEnabled()) module.renderHud()
                RenderEvent.Type.WORLD -> for(module in MODULES) if(module.isEnabled()) module.renderWorld()
            }
        }

        @field:EventHandler
        private val motionEventListener = EventListener<TickEvent.Motion> { event ->
            if(event.era == MeshEvent.Era.BEFORE)
                for(module in MODULES) if(module.isEnabled()) module.motion()
        }
    }

    override fun init() {
        val start = System.currentTimeMillis()

        // register companion object for basic module / manager events
        MESH.eventManager.register(Ares::class.java)

        // load managers into classpath
        RotationManager

        // load modules into classpath
        ESP
        TestModule

        // save settings on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            SETTINGS.write(SETTINGS_FILE)
        })

        LOGGER.info("Ares loaded {} modules and {} managers in {} milliseconds", MODULES.size, MANAGERS.size, System.currentTimeMillis() - start)
    }
}