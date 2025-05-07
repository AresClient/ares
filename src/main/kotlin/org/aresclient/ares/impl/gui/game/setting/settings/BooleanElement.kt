package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.impl.gui.game.setting.RowSettingElement

class BooleanElement(setting: BooleanSetting, scale: Float): RowSettingElement<BooleanSetting, Boolean>(setting, scale) {
    private val button = BooleanToggleButton(setting, scale)

    init {
        pushChild(RowButton(this) { button.click() })
        pushChild(button)
    }

    private class BooleanToggleButton(private val setting: BooleanSetting, height: Float): SubToggleButton(height) {
        override fun getState(): Boolean = setting.value

        override fun setState(value: Boolean) {
            setting.value = value
        }
    }
}
