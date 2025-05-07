package org.aresclient.ares.impl.gui.game.window

import org.aresclient.ares.api.gui.StaticElement
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.settings.list.MapListSetting

class WindowManager(private val settings: MapListSetting): StaticElement() {
    init {
        settings.forEach { map ->
            pushChild(WindowElement(map, this))
        }
    }

    fun <T: WindowContent> open(creator: MapSetting.() -> Class<T>?) {
        val map = MapSetting()
        settings.add(map)
        pushChild(WindowElement(map, this).also {
            it.open(creator)
        })
    }

    fun close(window: WindowElement) {
        settings.remove(window.settings)
        getChildren().remove(window)
    }

    fun float(window: WindowElement) {
        removeChild(window)
        pushChild(window)
    }
}