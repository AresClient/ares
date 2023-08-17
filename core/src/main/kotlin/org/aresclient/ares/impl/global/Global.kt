package org.aresclient.ares.impl.global

import org.aresclient.ares.impl.AresPlugin

open class Global(val name: String) {
    companion object {
        private val SETTINGS = AresPlugin.INSTANCE.settings.addMap("Globals")
    }

    protected val settings = SETTINGS.addMap(name)

    open fun tick() {
    }
}
