package org.aresclient.ares.impl.gui.game.setting.settings.number

import org.aresclient.ares.api.setting.settings.number.IntegerSetting
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class IntElement(setting: IntegerSetting, defaultHeight: Float): NumberElement<Int>(setting, defaultHeight) {
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
