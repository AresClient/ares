package org.aresclient.ares.global

import org.aresclient.ares.Ares

open class Global(val name: String) {
    companion object {
        val SETTINGS = org.aresclient.ares.Ares.SETTINGS.category("Globals")
        internal val MC = Ares.INSTANCE.minecraft
    }

    protected val settings = SETTINGS.category(name)

    init {
        Ares.GLOBALS.add(this)
    }

    open fun tick() {
    }
}
