package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.RangeValues
import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// TODO: DONT CONVERT TO DOUBLE
abstract class NumberElement<T: Number>(private val setting: Setting<T>): SettingElement({}) {
    protected val range = setting.possibleValues as RangeValues
    // 0 = max && min != null    1 = max && min == null
    // 2 = min == null           3 = max == null
    protected val mode = if(range.max == null || range.min == null) (if(range.min == null) (if(range.max == null) 1 else 2) else 3 ) else 0

    private var mouse = false
    private var time = 0L
    private var multiplier = 1.0

    private val minusText = "- " + setting.getName()

    abstract fun increment(value: Double)
    private fun increment(mouseX: Int) = increment((mouseX - getRenderX() - getWidth() / 2.0) / getWidth().toDouble() * 4.0 * multiplier)

    abstract fun percent(value: Float)
    abstract fun formatted(): String

    override fun getText(): String {
        return when(mode) {
            1, 2 -> minusText
            3 -> if(setting.value == range.min) setting.getName() else minusText
            else -> setting.getName()
        }
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(!acted.get() && isMouseOver(mouseX, mouseY)) {
            if(mouseButton == 0) {
                multiplier = 1.0
                mouse = true
                time = System.currentTimeMillis()
                if(mode > 0) increment(mouseX)
            } else if(mouseButton == 1) {
                setting.default()
            }

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
                    0f, getHeight() - 2, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    (setting.value.toFloat() - range.min!!.toFloat()) / (range.max!!.toFloat() - range.min.toFloat()) * getWidth(), getHeight() - 2, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
                )
                indices(0, 1)
            }
        } else if(mouse) {
            val diff = System.currentTimeMillis() - time
            if(diff > 500) {
                multiplier += 0.1
                increment(mouseX)
                time = 0
            }
        }

        var string = formatted()
        if(mode == 1 || mode == 3 || (mode == 2 && setting.value != range.max)) string += " +"
        fontRenderer.drawString(
            matrixStack, string, getWidth() - fontRenderer.getStringWidth(string) - 2, 1f,
            theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    protected fun round(string: String): String {
        return string.substring(0, min(string.length, string.indexOf('.') + 3))
    }
}

class DoubleElement(private val setting: Setting<Double>): NumberElement<Double>(setting) {
    override fun increment(value: Double) {
        val num = setting.value + value
        when(mode) {
            1 -> setting.value = num
            2 -> setting.value = min(range.max!!, num)
            3 -> setting.value = max(range.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = (value * (range.max!! - range.min!!)) + range.min
    }

    override fun formatted(): String = round(setting.value.toString())
}

class FloatElement(private val setting: Setting<Float>): NumberElement<Float>(setting) {
    override fun increment(value: Double) {
        val num = setting.value + value.toFloat()
        when(mode) {
            1 -> setting.value = num
            2 -> setting.value = min(range.max!!, num)
            3 -> setting.value = max(range.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = (value * (range.max!! - range.min!!)) + range.min
    }

    override fun formatted(): String = round(setting.value.toString())
}

class IntElement(private val setting: Setting<Int>): NumberElement<Int>(setting) {
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
            2 -> setting.value = min(range.max!!, num)
            3 -> setting.value = max(range.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = ((value * (range.max!! - range.min!!)) + range.min).toInt()
    }

    override fun formatted(): String = setting.value.toString()
}

class LongElement(private val setting: Setting<Long>): NumberElement<Long>(setting) {
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
            2 -> setting.value = min(range.max!!, num)
            3 -> setting.value = max(range.min!!, num)
        }
    }

    override fun percent(value: Float) {
        setting.value = ((value * (range.max!! - range.min!!)) + range.min).toLong()
    }

    override fun formatted(): String = setting.value.toString()
}
