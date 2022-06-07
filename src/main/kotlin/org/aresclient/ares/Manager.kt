package org.aresclient.ares

open class Manager {
    companion object {
        val MC = Ares.MESH.minecraft
        val RENDERER = Ares.MESH.renderer
    }

    init {
        Ares.MANAGERS.add(this)
    }

    open fun tick() {
    }
}