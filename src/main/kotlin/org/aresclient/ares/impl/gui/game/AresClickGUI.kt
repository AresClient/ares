package org.aresclient.ares.impl.gui.game

import org.aresclient.ares.api.render.BlurFrameBuffer
import org.aresclient.ares.api.gui.ScreenElement
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.SettingGroup
import org.aresclient.ares.impl.gui.AresSkybox

class AresClickGUI(settings: SettingGroup): ScreenElement("Ares ClickGUI") {
    private val windowManager = WindowManager(settings.addList("Windows"))
    private val navigationBar = NavigationBar(windowManager, 30f)
    private var blur: BlurFrameBuffer? = null

    init {
        pushChild(navigationBar)
        pushChild(windowManager)
    }

    override fun update() {
        if(MC.world == null) AresSkybox.update(getWidth(), getHeight())

        if(blur == null) blur = BlurFrameBuffer(MC.window)
        else blur!!.resize(MC.window)

        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(MC.world == null) AresSkybox.draw(delta)

        blur?.render(1f, 1f)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
