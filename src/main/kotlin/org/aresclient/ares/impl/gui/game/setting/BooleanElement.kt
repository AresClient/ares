package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.impl.gui.game.SettingElement

class BooleanElement(setting: BooleanSetting, scale: Float): SettingElement<BooleanSetting>(setting, scale) {
    private val button = BooleanToggleButton(setting, scale)

    init {
        pushChild(button)
        pushChild(SettingElementButton(this) { button.click() })
    }

    private class BooleanToggleButton(private val setting: BooleanSetting, height: Float): SettingSubToggleButton(height) {
        override fun getState(): Boolean = setting.value

        override fun setState(value: Boolean) {
            setting.value = value
        }
    }
}