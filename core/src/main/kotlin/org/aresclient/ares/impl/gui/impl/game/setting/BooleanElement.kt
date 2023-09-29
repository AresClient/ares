package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.game.SettingElement

class BooleanElement(setting: Setting.Boolean, scale: Float): SettingElement<Setting.Boolean>(setting, scale) {
    private val button = BooleanToggleButton(setting, scale)

    init {
        pushChild(button)
        pushChild(SettingElementButton(this) { button.click() })
    }

    private class BooleanToggleButton(private val setting: Setting.Boolean, height: Float): SettingSubToggleButton(height) {
        override fun getState(): Boolean = setting.value

        override fun setState(value: Boolean) {
            setting.value = value
        }
    }
}