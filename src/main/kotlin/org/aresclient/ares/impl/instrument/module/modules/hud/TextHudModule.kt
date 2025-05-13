package org.aresclient.ares.impl.instrument.module.modules.hud

import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color

abstract class TextHudModule(name: String, description: String, defaults: Defaults = Defaults(),
                             position: Pair<Double, Double> = 0.0 to 0.0): HudModule(name, description, defaults, position) {
    private val size = settings.addFloat("Size", 24f).setMin(5f).setMax(50f)
    private var text: String? = null

    abstract fun getText(): String?

    override fun getWidth() = getFontRenderer().getStringWidth(text ?: "", size.value) + 2f

    override fun getHeight() = getFontRenderer().getCharHeight(size.value) + 2f

    override fun onRenderHud(delta: Float, renderer: Renderer.State) {
        if(text == null) text = getText() ?: return
        getFontRenderer().drawString(renderer.matrixStack, text!!, size.value, getX() + 1, getY() + 1, Color.WHITE)
    }

    protected fun update() {
        text = getText()
    }
}
