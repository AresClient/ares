package org.aresclient.ares.impl.gui.game.setting.settings.number

import org.aresclient.ares.api.setting.settings.number.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class LongElement(setting: LongSetting, defaultHeight: Float): NumberElement<Long>(setting, defaultHeight) {
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
