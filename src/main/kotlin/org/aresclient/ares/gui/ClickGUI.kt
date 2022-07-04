package org.aresclient.ares.gui

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.gui.api.ScreenElement
import org.aresclient.ares.gui.api.Window
import org.aresclient.ares.gui.api.WorkingArea

class ClickGUI: ScreenElement("Ares ClickGUI") {
    companion object {
        val settings = Ares.SETTINGS.category("gui")
        val bind = settings.integer("bind", Keys.DOWN)
        val padding = settings.integer("padding", 3)
    }

    init {
        pushChild(Window("Test Window", WorkingArea(), isWindowOpen = { true }))
    }
}