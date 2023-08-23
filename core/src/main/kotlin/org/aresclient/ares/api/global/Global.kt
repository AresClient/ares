package org.aresclient.ares.api.global

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.setting.Setting

/**
 * Globals are used for listeners and groups of settings held in common between multiple
 * modules, and also hold related utility functions to the purpose of the Global
 */
open class Global(val name: String) {
    companion object {
        private val SETTINGS = Ares.getSettings().addMap("Globals")
    }

    protected val settings: Setting.Map<out Any> = SETTINGS.addMap(name)

    open fun tick() {
    }
}
