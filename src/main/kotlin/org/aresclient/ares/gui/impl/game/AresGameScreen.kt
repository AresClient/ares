package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.Settings
import org.aresclient.ares.gui.api.ScreenElement
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Texture
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class AresGameScreen: ScreenElement("Ares Game Screen") {
    companion object {
        val SETTINGS = Ares.SETTINGS.category("ClickGUI")
        val BIND = SETTINGS.integer("bind", Keys.DOWN)
    }

    private val windows = ArrayList<SettingsWindow>()

    init {
        Ares.SETTINGS.initWindow()
        windows.add(Ares.SETTINGS.window!!)

        Module.SETTINGS.map.values.forEach {
            if(it is Settings) {
                it.initWindow()
                windows.add(it.window!!)
            }
        }
    }
    private val navigationBar = NavigationBar(38f, windows)

    override fun init() {
        pushChild(navigationBar)
        initWindows(Ares.SETTINGS)
    }

    private fun initWindows(settings: Settings) {
        settings.initWindow()
        pushChild(settings.window!!)
        settings.map.values.forEach {
            if(it is Settings && it != Window.SETTINGS) initWindows(it)
        }
    }

    private fun Settings.initWindow() {
        if(window != null) return

        window = SettingsWindow(
            this,
            if(getParent() == Ares.SETTINGS.get("Modules")) Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/categories/${getName().lowercase()}.png"), false)
            else Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares_fg.png"), false),
            { 130f }, { expanded -> if(expanded) 300f else 0f }
        )
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
