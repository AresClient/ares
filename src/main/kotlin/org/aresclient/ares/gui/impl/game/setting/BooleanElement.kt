package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement

class BooleanElement(private val setting: Setting<Boolean>, defaultHeight: Float): SettingElement(defaultHeight) {
    private val button = BooleanToggleButton(setting, defaultHeight)

    init {
        pushChild(button)
        pushChild(SettingElementButton(this) { button.click() })
    }

    override fun getText(): String = setting.getName()

    private class BooleanToggleButton(private val setting: Setting<Boolean>, height: Float): SettingSubToggleButton(height) {
        override fun getState(): Boolean = setting.value

        override fun setState(value: Boolean) {
            setting.value = value
        }
    }
}