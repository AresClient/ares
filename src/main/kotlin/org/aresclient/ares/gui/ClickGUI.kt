package org.aresclient.ares.gui

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.gui.api.ScreenElement
import org.aresclient.ares.gui.api.Window
import org.aresclient.ares.gui.api.WorkingArea
import org.aresclient.ares.renderer.MatrixStack

class ClickGUI: ScreenElement("Ares ClickGUI") {
    companion object {
        val settings = Ares.SETTINGS.category("gui")
        val bind = settings.integer("bind", Keys.DOWN)
        val padding = settings.integer("padding", 3)
    }

    private lateinit var matrices: MatrixStack
    var first = true

    override fun init() {
        matrices = MatrixStack().also {
            it.projection().ortho(0F, width.toFloat(), height.toFloat(), 0F, 0F, 1F)
        }

        if(first) {
            pushChild(Window("Test Window", this, matrices, WorkingArea(matrices), isWindowOpen = { true }))
            first = false
        }
    }

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    override fun onClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    override fun onRelease(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    override fun onType(typedChar: Char?, keyCode: Int) {
    }

    override fun onScroll(mouseX: Double, mouseY: Double, value: Double) {
    }
}