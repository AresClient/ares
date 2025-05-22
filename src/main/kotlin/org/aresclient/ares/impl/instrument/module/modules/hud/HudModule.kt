package org.aresclient.ares.impl.instrument.module.modules.hud

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.FontRenderer
import org.aresclient.ares.impl.util.Theme
import java.awt.Font

abstract class HudModule(name: String, description: String, defaults: Defaults = Defaults(),
                         position: Pair<Double, Double> = 0.0 to 0.0): Module(Category.HUD, name, description, defaults) {
    init {
        // kinda a hack, but we don't want any hud modules to show on module list because you can already see them
        defaults.setExternalModuleList(false)
    }

    private val x = settings.addDouble("X", position.first).setMin(0.0).setMax(1.0)
    private val y = settings.addDouble("Y", position.second).setMin(0.0).setMax(1.0)

    abstract fun getWidth(): Float

    abstract fun getHeight(): Float

    fun getX(): Float = (x.value * (MC.window.framebufferWidth - getWidth()).toDouble()).toFloat()

    fun setX(value: Float) {
        x.value = value.toDouble() / (MC.window.framebufferWidth - getWidth()).toDouble()
    }

    fun getY(): Float = (y.value * (MC.window.framebufferHeight - getHeight())).toFloat()

    fun setY(value: Float) {
        y.value = value.toDouble() / (MC.window.framebufferHeight - getHeight()).toDouble()
    }

    protected fun getFontRenderer(style: Int = Font.PLAIN): FontRenderer {
        return Theme.current().font.value.getRenderer(style)
    }
}
