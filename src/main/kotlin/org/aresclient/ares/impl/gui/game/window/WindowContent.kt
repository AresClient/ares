package org.aresclient.ares.impl.gui.game.window

import org.aresclient.ares.api.gui.StaticElement
import org.aresclient.ares.api.render.Texture
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.impl.gui.game.NavigationBar

abstract class WindowContent(internal val settings: MapSetting): StaticElement() {
    companion object {
        val DEFAULT_ICON = Texture(NavigationBar::class.java.getResourceAsStream("/assets/ares/textures/icons/gears.png"))
    }

    abstract fun getTitle(): String

    open fun getIcon(): Texture = DEFAULT_ICON

    fun getWindow() = getParent() as? WindowElement

    override fun getWidth() = getParent()?.getWidth() ?: 0f
}
