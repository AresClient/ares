package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.gui.api.ScreenElement
import org.aresclient.ares.module.Category
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Texture
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class AresGameScreen: ScreenElement("Ares Game Screen") {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("gui")
        val BIND = SETTINGS.integer("bind", Keys.DOWN)
    }

    private val windows = Category.values().map {
        val name = it.name.lowercase()
        Window(
            name.replaceFirstChar { it.uppercase() },
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/categories/$name.png"), false),
            { 130f }, { expanded -> if(expanded) 300f else 0f }
        )
    }
    private val navigationBar = NavigationBar(38f, windows)

    override fun init() {
        pushChild(navigationBar)
        pushChildren(windows)
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

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for(element in getChildren().reversed()) {
            if(!element.isVisible() || element !is Window) continue
            else if(element.isMouseOver(mouseX, mouseY)) {
                removeChild(element)
                pushChild(element)
                element.click(mouseX, mouseY, mouseButton)
                return
            }
        }

        if(navigationBar.isMouseOver(mouseX, mouseY))
            navigationBar.click(mouseX, mouseY, mouseButton)
    }
}
