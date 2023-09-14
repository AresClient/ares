package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.render.BlurFrameBuffer
import org.aresclient.ares.impl.gui.api.ScreenElement
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.AresSkybox

class AresClickGUI(settings: Setting.Map<*>): ScreenElement("Ares ClickGUI") {
    private val windowManager = WindowManager(settings.addList(Setting.Type.MAP, "Windows"))
    private val navigationBar = NavigationBar(windowManager, 30f)
    private var blur: BlurFrameBuffer? = null

    init {
        pushChild(navigationBar)
        pushChild(windowManager)
    }

    override fun update() {
        if(!Ares.getMinecraft().isInWorld) AresSkybox.update(getWidth(), getHeight())

        if(blur == null) blur = BlurFrameBuffer(Ares.getMinecraft().resolution)
        else blur!!.resize(Ares.getMinecraft().resolution)

        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(!Ares.getMinecraft().isInWorld) AresSkybox.draw(delta)

        blur?.render(1f, 1f)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
