package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Theme

class EnumElement<T: Enum<*>>(private val setting: Setting<T>): SettingElement({
    setting.value = setting.value.javaClass.enumConstants[(setting.value.ordinal + 1) % setting.value.javaClass.enumConstants.size]
    (it as EnumElement<*>).updateName()
}) {
    private var name = setting.value.name.formatToPretty()

    override fun getText(): String = setting.getName()

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        fontRenderer.drawString(
            matrixStack, name, getWidth() - fontRenderer.getStringWidth(name) - 2, 1f,
            theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    private fun updateName() {
        name = setting.value.name.formatToPretty()
    }
}
