package org.aresclient.ares.impl.instrument.modules.hud

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.aresclient.ares.api.nrender.hud.HudDrawer
import org.aresclient.ares.api.util.Color
import org.joml.Vector2d

abstract class TextHudModule(name: String, description: String, defaults: Defaults = Defaults(),
                             position: Vector2d = Vector2d(0.0, 0.0)): HudModule(name, description, defaults, position) {
    private var text: Text? = null

    abstract fun getText(): Text?

    override fun getWidth(): Float {
        if(text == null) return 0f
        return getFont().getWidth(text!!, getSize()) + 2f
    }

    override fun getHeight(): Float {
        return getFont().getHeight(getSize()) + 2f
    }

    override fun onRenderHud(drawer: HudDrawer, matrixStack: MatrixStack, delta: Float) {
        if(text == null) text = getText() ?: return
        drawer.drawText(getFont(), text!!, matrixStack, getX() + 1f, getY() + 1f, Color.WHITE, size = getSize())
    }

    protected fun update() {
        text = getText()
    }
}
