package org.aresclient.ares

import net.meshmc.mesh.Mesh
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
    }

    override fun init() {
        val start = System.currentTimeMillis()

        // register modules for events
        MESH.eventManager.register(Module::class.java)

        // load modules into classpath
        ESP
        TestModule

        // save settings on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            SETTINGS.write(SETTINGS_FILE)
        })

        LOGGER.info("Ares loaded {} modules in {} milliseconds", Module.MODULES.size, System.currentTimeMillis() - start)
    }
}