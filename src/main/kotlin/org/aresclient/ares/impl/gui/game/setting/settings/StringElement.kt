package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.api.gui.TextBox
import org.aresclient.ares.api.setting.settings.StringSetting
import org.aresclient.ares.impl.gui.game.setting.RowSettingElement

class StringElement(setting: StringSetting, scale: Float): RowSettingElement<StringSetting, String>(setting, scale) {
    private val textBox = SettingTextBox(this)

    init {
        textBox.setText(setting.value)
        textBox.setCursor(setting.value.length)
        pushChild(RowButton(this) {
            textBox.setFocused(true)
        })
        pushChild(textBox)
    }

    override fun getHeight(): Float = textBox.getHeight() + textBox.getY() * 2

    override fun change() {
        textBox.setText(setting.value)
    }

    private class SettingTextBox(private val element: StringElement, size: Float = 0.7f):
			TextBox(0f, 0f, 75f, element.fontRenderer.fontSize * size, 1, horizPadFactor = 0.2f) {
        private val offset = (1f - size) / 2f
        private val left = element.fontRenderer.getStringWidth(element.setting.name) + 6f

        override fun getX(): Float = element.getWidth() - getY() - getWidth()

        override fun getY(): Float = element.fontRenderer.fontSize * offset

        override fun getWidth(): Float = element.getWidth() - left - getY()

        override fun type(typedChar: Char?, keyCode: Int) {
            super.type(typedChar, keyCode)
            if(isFocused()) element.setting.value = getText()
        }
    }
}