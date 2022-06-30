package org.aresclient.ares.module

import org.aresclient.ares.Ares
import org.aresclient.ares.manager.RenderGlobalEvent

abstract class Module(val name: String, val description: String, val category: Category, enabled: Boolean = false,
                  bind: Int = 0, visible: Boolean = true, private val alwaysListening: Boolean = false, val priority: Int = -1) {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("modules")
        val MC = Ares.MESH.minecraft
    }

    protected val settings = SETTINGS.category(name)
    private var enabled = settings.boolean("enabled", enabled)
    private var bind = settings.integer("bind", bind)
    private var visible = settings.boolean("visible", visible)

    enum class TogglesWhen {
        PRESSED,
        RELEASED,
        HELD_DOWN
    }

    private var toggleState = settings.enum("toggle when", TogglesWhen.PRESSED)
    var pressed = false

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
        if(shouldTick()) Ares.MESH.eventManager.register(this)
    }

    fun tick() {
        if(shouldTick()) onTick()
    }

    fun renderHud(delta: Float) {
        if(shouldTick()) onRenderHud(delta)
    }

    fun renderWorld(event: RenderGlobalEvent) {
        if(shouldTick()) onRenderWorld(event)
    }

    fun motion() {
        if(shouldTick()) onMotion()
    }

    fun enable() {
        enabled.value = true
        if(!alwaysListening) Ares.MESH.eventManager.register(this)
        onEnable()
    }

    fun disable() {
        enabled.value = false
        if(!alwaysListening) Ares.MESH.eventManager.unregister(this)
        onDisable()
    }

    protected open fun onTick() {
    }

    protected open fun onRenderHud(delta: Float) {
    }

    protected open fun onRenderWorld(event: RenderGlobalEvent) {
    }

    protected open fun onMotion() {
    }

    protected open fun onEnable() {
    }

    protected open fun onDisable() {
    }

    fun setEnabled(value: Boolean) {
        if(value && !enabled.value) enable()
        else if(!value && enabled.value) disable()
    }

    fun toggle() {
        if(enabled.value) disable()
        else enable()
    }

    private fun shouldTick(): Boolean {
        if(enabled.value || alwaysListening) return true
        return false
    }
}
