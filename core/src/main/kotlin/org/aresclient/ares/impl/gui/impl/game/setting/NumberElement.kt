package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.impl.gui.impl.game.SettingElement
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// TODO: DONT CONVERT TO DOUBLE
abstract class NumberElement<T: Number>(setting: Setting.Number<T>, scale: Float): SettingElement<Setting.Number<T>>(setting, scale) {
    // 0 = max && min != null    1 = max && min == null
    // 2 = min == null           3 = max == null
    protected val mode = if(setting.max == null || setting.min == null) (if(setting.min == null) (if(setting.max == null) 1 else 2) else 3 ) else 0

    private var mouse = false
    private var time = 0L
    private var multiplier = 1.0

    private val minusText = "- " + setting.name

    abstract fun increment(value: Double)
    private fun increment(mouseX: Int) = increment((mouseX - getRenderX() - getWidth() / 2.0) / getWidth().toDouble() * 4.0 * multiplier)

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

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(mouseButton == 0 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            multiplier = 1.0
            mouse = true
            time = System.currentTimeMillis()
            if(mode > 0) increment(mouseX)

            acted.set(true)
        }
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
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
                increment(mouseX)
                time = 0
            }
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    protected fun round(string: String): String {
        return string.substring(0, min(string.length, string.indexOf('.') + 3))
    }
}

class DoubleElement(setting: Setting.Double, defaultHeight: Float): NumberElement<Double>(setting, defaultHeight) {
    override fun increment(value: Double) {
        val num = setting.value + value
        when(mode) {
            1 -> setting.value = num
            2 -> setting.value = min(setting.max!!, num)
            3 -> setting.value = max(setting.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = (value * (setting.max!! - setting.min!!)) + setting.min
    }

    override fun formatted(): String = round(setting.value.toString())
}

class FloatElement(setting: Setting.Float, defaultHeight: Float): NumberElement<Float>(setting, defaultHeight) {
    override fun increment(value: Double) {
        val num = setting.value + value.toFloat()
        when(mode) {
            1 -> setting.value = num
            2 -> setting.value = min(setting.max!!, num)
            3 -> setting.value = max(setting.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = (value * (setting.max!! - setting.min!!)) + setting.min
    }

    override fun formatted(): String = round(setting.value.toString())
}

class IntElement(setting: Setting.Integer, defaultHeight: Float): NumberElement<Int>(setting, defaultHeight) {
    private fun addClamp(a: Int, b: Int): Int {
        val sum = a + b
        return if(((a xor sum) and (b xor sum)) < 0) {
            val n = if(abs(a) > abs(b)) a else b
            return if(n > 0) Int.MAX_VALUE
            else Int.MIN_VALUE
        } else sum
    }

    override fun increment(value: Double) {
        val num = addClamp(setting.value, value.toInt())
        when(mode) {
            1 -> setting.value = num
            2 -> setting.value = min(setting.max!!, num)
            3 -> setting.value = max(setting.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = ((value * (setting.max!! - setting.min!!)) + setting.min).toInt()
    }

    override fun formatted(): String = setting.value.toString()
}

class LongElement(setting: Setting.Long, defaultHeight: Float): NumberElement<Long>(setting, defaultHeight) {
    private fun addClamp(a: Long, b: Long): Long {
        val sum = a + b
        return if(((a xor sum) and (b xor sum)) < 0L) {
            val n = if(abs(a) > abs(b)) a else b
            return if(n > 0L) Long.MAX_VALUE
            else Long.MIN_VALUE
        } else sum
    }

    override fun increment(value: Double) {
        val num = addClamp(setting.value, value.toLong())
        when(mode) {
            1 -> setting.value = num
            2 -> setting.value = min(setting.max!!, num)
            3 -> setting.value = max(setting.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = ((value * (setting.max!! - setting.min!!)) + setting.min).toLong()
    }

    override fun formatted(): String = setting.value.toString()
}
