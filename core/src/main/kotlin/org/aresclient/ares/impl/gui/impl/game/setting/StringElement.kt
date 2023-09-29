package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.api.TextBox
import org.aresclient.ares.impl.gui.impl.game.SettingElement

class StringElement(setting: Setting.String, scale: Float): SettingElement<Setting.String>(setting, scale) {
    private val textBox = SettingTextBox(this)

    init {
        textBox.setText(setting.value)
        textBox.setCursor(setting.value.length)
        pushChild(SettingElementButton(this) {
            textBox.setFocused(true)
        })
        pushChild(textBox)
    }

    override fun getHeight(): Float = textBox.getHeight() + textBox.getY() * 2

    private class SettingTextBox(private val element: StringElement, size: Float = 0.7f):
        TextBox(0f, 0f, 75f, element.fontRenderer.fontSize * size, 1, horizPadFactor = 0.2f) {
        private val offset = (1f - size) / 2f
        private val left = element.fontRenderer.getStringWidth(element.setting.name) + 6f

        override fun getX(): Float = element.getWidth() - getY() - getWidth()

        override fun getY(): Float = element.fontRenderer.fontSize * offset

        override fun getWidth(): Float = element.getWidth() - left - getY()

        override fun type(typedChar: Char?, keyCode: Int) {
            super.type(typedChar, keyCode)
            element.setting.value = getText()
        }
    }
}