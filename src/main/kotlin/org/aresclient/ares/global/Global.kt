package org.aresclient.ares.global

import org.aresclient.ares.Ares

open class Global(val name: String) {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("globals")
        internal val MC = Ares.MESH.minecraft
    }

    protected val settings = SETTINGS.category(name)

    init {
        Ares.GLOBALS.add(this)
    }

    open fun tick() {
    }
}