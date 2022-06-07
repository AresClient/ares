package org.aresclient.ares.module

import net.meshmc.mesh.event.events.client.InputEvent
import org.aresclient.ares.Ares

open class Module(val name: String, val description: String, val category: Category, enabled: Boolean = false,
                  bind: Int = 0, visible: Boolean = true, private val alwaysListening: Boolean = false, val priority: Int = -1) {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("modules")

        val MC = Ares.MESH.minecraft
        val RENDERER = Ares.MESH.renderer
    }

    protected val settings = SETTINGS.category(name)
    private var enabled = settings.boolean("enabled", enabled)
    private var bind = settings.integer("bind", bind)
    private var visible = settings.boolean("visible", visible)

    // released is mechanically slower to activate but preferable in general as it doesn't cause spam when held down
    private var toggleState = settings.enum("toggle state", InputEvent.Keyboard.State.RELEASED)

    fun isEnabled() = enabled.value
    fun getBind() = bind.value
    fun isVisible() = visible.value

    fun getToggleState() = toggleState.value

    fun setBind(value: Int) {
        bind.value = value
    }

    init {
        Ares.MODULES.add(this)
        category.modules.add(this)
    }

    fun postInit() {
        if(isEnabled() || alwaysListening) Ares.MESH.eventManager.register(this)
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

    fun toggle() {
        if(enabled.value) disable()
        else enable()
    }
}
