package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Setting
import org.aresclient.ares.gui.api.TextBox
import org.aresclient.ares.gui.impl.game.SettingElement

class StringElement(private val setting: Setting<String>, defaultHeight: Float): SettingElement(defaultHeight) {
    private val textBox = SettingTextBox(this)

    init {
        textBox.setText(setting.value)
        textBox.setCursor(setting.value.length)
        pushChild(textBox)
    }

    override fun getText(): String = setting.getName()

    override fun getHeight(): Float = textBox.getHeight() + textBox.getY() * 2

    private class SettingTextBox(private val element: StringElement, size: Float = 0.7f):
        TextBox(0f, 0f, 75f, element.fontRenderer.fontSize * size, 1, horzPadFactor = 0.2f) {
        private val offset = (1f - size) / 2f
        private val left = element.fontRenderer.getStringWidth(element.setting.getName()) + 6f

        override fun getX(): Float = element.getWidth() - getY() - getWidth()

        override fun getY(): Float = element.fontRenderer.fontSize * offset

        override fun getWidth(): Float = element.getWidth() - left - getY()

        override fun type(typedChar: Char?, keyCode: Int) {
            super.type(typedChar, keyCode)
            element.setting.value = getText()
        }
    }
}