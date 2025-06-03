package org.aresclient.ares.impl.instrument.modules.hud

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.font.CustomFont
import org.aresclient.ares.impl.util.Theme
import org.joml.Vector2d

abstract class HudModule(name: String, description: String, defaults: Defaults = Defaults(),
                         position: Vector2d = Vector2d(0.0, 0.0)): Module(Category.HUD, name, description, defaults.setExternalModuleList(false)) {
    private val x = settings.addDouble("X", position.x).setMin(0.0).setMax(1.0)
    private val y = settings.addDouble("Y", position.y).setMin(0.0).setMax(1.0)
    private val size = settings.addFloat("Size", 11f).setMin(5f).setMax(22f)

    abstract fun getWidth(): Float

    abstract fun getHeight(): Float

    fun getX(): Float {
        return (x.value * (MC.window.scaledWidth - getWidth()).toDouble()).toFloat()
    }

    fun setX(value: Float) {
        x.value = value.toDouble() / (MC.window.scaledWidth - getWidth()).toDouble()
    }

    fun getY(): Float {
        return (y.value * (MC.window.scaledHeight - getHeight())).toFloat()
    }

    fun setY(value: Float) {
        y.value = value.toDouble() / (MC.window.scaledHeight - getHeight()).toDouble()
    }

    protected fun getSize(): Float = size.value

    protected fun getFont(): CustomFont = Theme.current().nfont.value
}
