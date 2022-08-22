package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingSubToggleButton

class BooleanElement(setting: Setting<Boolean>): SettingElement(setting.getName(), {
    (it as BooleanElement).button.click()
}) {
    private val button = BooleanToggleButton(setting)

    init {
        pushChild(button)
    }

    private class BooleanToggleButton(private val setting: Setting<Boolean>): SettingSubToggleButton() {
        override fun getState(): Boolean = setting.value

        override fun setState(value: Boolean) {
            setting.value = value
        }
    }
}