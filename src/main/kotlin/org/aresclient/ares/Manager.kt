package org.aresclient.ares

abstract class Manager(val name: String) {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("managers")

        val MC = Ares.MESH.minecraft
        val RENDERER = Ares.MESH.renderer
    }

    protected val settings = SETTINGS.category(name)

    init {
        Ares.MANAGERS.add(this)
    }

    open fun tick() {
    }
}