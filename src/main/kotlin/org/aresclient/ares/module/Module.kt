package org.aresclient.ares.module

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.client.TickEvent
import net.meshmc.mesh.event.events.render.RenderEvent
import org.aresclient.ares.Ares

open class Module(val name: String, val description: String, val category: Category, enabled: Boolean = false,
                  bind: Int = 0, visible: Boolean = true, private val alwaysListening: Boolean = false) {
    companion object {
        val MODULES = arrayListOf<Module>()
        val SETTINGS = Ares.SETTINGS.category("modules")

        val MC = Ares.MESH.minecraft
        val RENDERER = Ares.MESH.renderer

        @field:EventHandler
        private val tickEventListener = EventListener<TickEvent.Client> { event ->
           if(event.era == MeshEvent.Era.BEFORE) MODULES.forEach(Module::tick)
        }

        @field:EventHandler
        private val renderEventListener = EventListener<RenderEvent> { event ->
            when(event.type) {
                RenderEvent.Type.HUD -> MODULES.forEach(Module::renderHud)
                RenderEvent.Type.WORLD -> MODULES.forEach(Module::renderWorld)
            }
        }

        @field:EventHandler
        private val motionEventListener = EventListener<TickEvent.Motion> { event ->
            if(event.era == MeshEvent.Era.BEFORE) MODULES.forEach(Module::motion)
        }
    }

    protected val settings = SETTINGS.category(name)
    private var enabled = settings.boolean("enabled", enabled)
    private var bind = settings.integer("bind", bind)
    private var visible = settings.boolean("visible", visible)

    fun isEnabled() = enabled.value
    fun getBind() = bind.value
    fun isVisible() = visible.value

    init {
        MODULES.add(this)
        category.modules.add(this)
        if(enabled || alwaysListening) Ares.MESH.eventManager.register(this)
    }

    open fun tick() {
    }

    open fun renderHud() {
    }

    open fun renderWorld() {
    }

    open fun motion() {
    }

    open fun enable() {
        enabled.value = true
        if(!alwaysListening) Ares.MESH.eventManager.register(this)
    }

    open fun disable() {
        enabled.value = false
        if(!alwaysListening) Ares.MESH.eventManager.unregister(this)
    }

    fun setEnabled(value: Boolean) {
        if(value && !enabled.value) enable()
        else if(!value && enabled.value) disable()
    }
}
