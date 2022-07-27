package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.Settings
import org.aresclient.ares.gui.api.ScreenElement
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Texture
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class AresGameScreen: WindowContext(SETTINGS, "Ares Game Screen") {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("ClickGUI")
        val BIND = SETTINGS.integer("bind", Keys.DOWN)
    }

    private val navigationBar = NavigationBar(this, 38f)

    override fun init() {
        pushChild(navigationBar)
        super.init()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.triangle.draw {
            vertices(
                -1f, -1f, 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.1f,
                1f, -1f, 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.1f,
                -1f, 1f, 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.1f,
                1f, 1f, 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.1f
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
