package org.aresclient.ares.impl.gui.game.setting.settings.number

import org.aresclient.ares.api.setting.settings.number.FloatSetting
import kotlin.math.max
import kotlin.math.min

class FloatElement(setting: FloatSetting, defaultHeight: Float): NumberElement<Float>(setting, defaultHeight) {
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
