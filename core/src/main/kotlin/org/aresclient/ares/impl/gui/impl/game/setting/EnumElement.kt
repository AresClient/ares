package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.impl.gui.impl.game.SettingElement
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting

class EnumElement<T: Enum<*>>(setting: Setting.Enum<T>, scale: Float): SettingElement<Setting.Enum<T>>(setting, scale) {
    private var text = setting.value.name.formatToPretty()

    init {
        pushChild(SettingElementButton(this) {
            setting.value = setting.value.javaClass.enumConstants[(setting.value.ordinal + 1) % setting.value.javaClass.enumConstants.size]
        })
    }

    override fun change() {
        text = setting.value.name.formatToPretty()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        fontRenderer.drawString(
            matrixStack, text, getWidth() - fontRenderer.getStringWidth(text) - 2, 1f,
            theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
