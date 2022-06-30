package org.aresclient.ares

abstract class Global(val name: String) {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("globals")

        val MC = Ares.MESH.minecraft
    }

    protected val settings = SETTINGS.category(name)

    init {
        Ares.GLOBALS.add(this)
    }

    open fun tick() {
    }
}