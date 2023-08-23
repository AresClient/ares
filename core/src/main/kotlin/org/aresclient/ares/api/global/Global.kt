package org.aresclient.ares.api.global

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.setting.Setting

/**
 * Globals are used for grouping settings and functions which interact with multiple
 * modules but do not themselves directly do anything, and also holds related utility
 * functions to the purpose of the Global
 */
open class Global(val name: String, val description: String) {
    companion object {
        private val SETTINGS = Ares.getSettings().addMap("Globals")
    }

    protected val settings: Setting.Map<out Any> = SETTINGS.addMap(name)

    open fun tick() {
    }
}
