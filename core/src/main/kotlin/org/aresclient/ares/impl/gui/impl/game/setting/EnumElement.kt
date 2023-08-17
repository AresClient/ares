package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.game.SettingElement
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer

class EnumElement<T: Enum<*>>(private val setting: Setting.Enum<T, *>, defaultHeight: Float): SettingElement(defaultHeight) {
    private var name = setting.value.name.formatToPretty()

    init {
        pushChild(SettingElementButton(this) {
            setting.value = setting.value.javaClass.enumConstants[(setting.value.ordinal + 1) % setting.value.javaClass.enumConstants.size]
            name = setting.value.name.formatToPretty()
        })
    }

    override fun getText(): String = setting.name

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        fontRenderer.drawString(
            matrixStack, name, getWidth() - fontRenderer.getStringWidth(name) - 2, 1f,
            theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
