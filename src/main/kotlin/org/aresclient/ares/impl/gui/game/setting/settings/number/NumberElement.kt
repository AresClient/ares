package org.aresclient.ares.impl.gui.game.setting.settings.number

import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.settings.number.NumberSetting
import org.aresclient.ares.impl.gui.game.setting.RowSettingElement
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

// TODO: DONT CONVERT TO DOUBLE
abstract class NumberElement<T: Number>(setting: NumberSetting<T>, scale: Float): RowSettingElement<NumberSetting<T>, T>(setting, scale) {
    // 0 = max && min != null    1 = max && min == null
    // 2 = min == null           3 = max == null
    protected val mode = if(setting.max == null || setting.min == null) (if(setting.min == null) (if(setting.max == null) 1 else 2) else 3 ) else 0

    private var mouse = false
    private var time = 0L
    private var multiplier = 1.0

    private val minusText = "- " + setting.name

    abstract fun increment(value: Double)
    private fun incrementFromMouse(mouseX: Double) = increment((mouseX - getRenderX() - getWidth() / 2.0) / getWidth().toDouble() * 4.0 * multiplier)

    abstract fun percent(value: Float)
    abstract fun formatted(): String

    override fun getText(): String {
        return when(mode) {
            1, 2 -> minusText
            3 -> if(setting.value == setting.min) setting.name else minusText
            else -> setting.name
        }
    }

    override fun getSecondaryText(): String? {
        var string = formatted()
        if(mode == 1 || mode == 3 || (mode == 2 && setting.value != setting.max)) string += " +"
        return string
    }

    override fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        if(mouseButton == 0 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            multiplier = 1.0
            mouse = true
            time = System.currentTimeMillis()
            if(mode > 0) incrementFromMouse(mouseX)

            acted.set(true)
        }
    }

    override fun release(mouseX: Double, mouseY: Double, mouseButton: Int) {
        mouse = false
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(mode == 0) {
            if(mouse) percent(((mouseX - getRenderX()) / getWidth()).coerceIn(0f, 1f))

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, getHeight() - 2, 0f, 3f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    (setting.value.toFloat() - setting.min!!.toFloat()) / (setting.max!!.toFloat() - setting.min.toFloat()) * getWidth(), getHeight() - 2, 0f, 3f,
                        theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(0, 1)
            }
        } else if(mouse) {
            val diff = System.currentTimeMillis() - time
            if(diff > 500) {
                multiplier += 0.05
                incrementFromMouse(mouseX.toDouble())
                time = 0
            }
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    protected fun round(string: String): String {
        return string.substring(0, min(string.length, string.indexOf('.') + 3))
    }
}
