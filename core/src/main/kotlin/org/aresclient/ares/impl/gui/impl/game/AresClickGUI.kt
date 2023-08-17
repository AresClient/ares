package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.api.ScreenElement
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer

class AresClickGUI<T>(settings: Setting.Map<T>): ScreenElement("Ares ClickGUI") {
    private val windowManager = WindowManager<T>(settings)
    private val navigationBar = NavigationBar(windowManager, 38f)

    init {
        pushChild(navigationBar)
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.triangle.draw {
            vertices(
                -1f, -1f, 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.1f,
                1f, -1f, 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.1f,
                -1f, 1f, 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.1f,
                1f, 1f, 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.1f
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
