package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingSubToggleButton

class BooleanElement(private val setting: Setting<Boolean>): SettingElement({
    (it as BooleanElement).button.click()
}) {
    private val button = BooleanToggleButton(setting)

    init {
        pushChild(button)
    }

    override fun getText(): String = setting.getName()

    private class BooleanToggleButton(private val setting: Setting<Boolean>): SettingSubToggleButton() {
        override fun getState(): Boolean = setting.value

        override fun setState(value: Boolean) {
            setting.value = value
        }
    }
}