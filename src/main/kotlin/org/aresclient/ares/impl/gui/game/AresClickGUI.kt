package org.aresclient.ares.impl.gui.game

import org.aresclient.ares.api.gui.ScreenElement
import org.aresclient.ares.api.render.BlurFramebuffer
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.impl.gui.AresSkybox
import org.aresclient.ares.impl.gui.game.window.WindowManager
import org.aresclient.ares.impl.util.Theme

class AresClickGUI(settings: MapSetting): ScreenElement("Ares ClickGUI") {
    private val windowManager = WindowManager(settings.addList("Windows"))
    private val navigationBar = NavigationBar(windowManager, 30f)
    private var blur: BlurFramebuffer? = null

    init {
        pushChild(navigationBar)
        pushChild(windowManager)
    }

    override fun update() {
        if(MC.NULL) AresSkybox.update(getWidth(), getHeight())

        if(blur == null) blur = BlurFramebuffer(MC.framebuffer.textureWidth, MC.framebuffer.textureHeight)
        else blur!!.resize(MC.framebuffer.textureWidth, MC.framebuffer.textureHeight)

        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(MC.NULL) AresSkybox.draw(delta)

        blur?.render(1f, 1f)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
