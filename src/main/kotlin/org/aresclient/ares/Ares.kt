package org.aresclient.ares

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.Mesh
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.client.TickEvent
import net.meshmc.mesh.event.events.render.RenderEvent
import org.apache.logging.log4j.LogManager
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

        val MODULES = linkedMapOf<Class<out Module>, Module>()
        val MANAGERS = linkedMapOf<Class<out Manager>, Manager>()

        // returns a module's instance
        fun <T> getModule(clazz: Class<T>): T where T: Module {
            return MODULES[clazz] as T
        }

        // returns a manager's instance
        fun <T> getManager(clazz: Class<T>): T where T: Manager {
            return MANAGERS[clazz] as T
        }

        @field:EventHandler
        private val tickEventListener = EventListener<TickEvent.Client> { event ->
            if(event.era == MeshEvent.Era.BEFORE) {
                MODULES.values.forEach(Module::tick)
                MANAGERS.values.forEach(Manager::tick)
            }
        }

        @field:EventHandler
        private val renderEventListener = EventListener<RenderEvent> { event ->
            when(event.type) {
                RenderEvent.Type.HUD -> MODULES.values.forEach(Module::renderHud)
                RenderEvent.Type.WORLD -> MODULES.values.forEach(Module::renderWorld)
            }
        }

        @field:EventHandler
        private val motionEventListener = EventListener<TickEvent.Motion> { event ->
            if(event.era == MeshEvent.Era.BEFORE) MODULES.values.forEach(Module::motion)
        }
    }

    override fun init() {
        val start = System.currentTimeMillis()

        // register companion object for basic module / manager events
        MESH.eventManager.register(Ares::class.java)

        // load modules into classpath
        ESP
        TestModule

        // save settings on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            SETTINGS.write(SETTINGS_FILE)
        })

        LOGGER.info("Ares loaded {} modules in {} milliseconds", MODULES.size, System.currentTimeMillis() - start)
    }
}