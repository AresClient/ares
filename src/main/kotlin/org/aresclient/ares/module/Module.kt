package org.aresclient.ares.module

import org.aresclient.ares.Ares
import org.aresclient.ares.global.RenderGlobal

abstract class Module(
    val name: String, val description: String, val category: Category, enabled: Boolean = false,
    bind: Int = 0, visible: Boolean = true, private val alwaysListening: Boolean = false) {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("Modules")
        val CATEGORIES = arrayOf(
            SETTINGS.category("Player"),
            SETTINGS.category("Combat"),
            SETTINGS.category("Movement"),
            SETTINGS.category("Render"),
            SETTINGS.category("Hud"),
            SETTINGS.category("Misc"),
        )
        val MC = Ares.MESH.minecraft
    }

    protected val settings = CATEGORIES[category.ordinal].category(name)
    private var enabled = settings.boolean("Enabled", enabled)
    private var visible = settings.boolean("Visible", visible)
    private var bind = settings.bind("Bind", bind)

    enum class TogglesWhen {
        PRESS,
        RELEASE,
        HOLD
    }

    private var toggleState = settings.enum("Toggle On", TogglesWhen.PRESS)
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
        if(shouldTick()) Ares.MESH.eventManager.register(this.javaClass)
    }

    fun tick() {
        if(shouldTick()) onTick()
    }

    fun renderHud(delta: Float) {
        if(shouldTick()) onRenderHud(delta)
    }

    fun renderWorld(event: RenderGlobal.Event) {
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

    protected open fun onRenderWorld(event: RenderGlobal.Event) {
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

    private fun shouldTick() = enabled.value || alwaysListening
}
