package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.gui.api.DynamicElement
import org.aresclient.ares.impl.gui.impl.game.DropDownSettingElement
import org.aresclient.ares.impl.gui.impl.game.formatToPretty
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme

class EnumElement<T: Enum<*>>(setting: Setting.Enum<T>, scale: Float): DropDownSettingElement<Setting.Enum<T>>(setting, scale) {
    private var text = setting.value.name.formatToPretty()

    init {
        pushChild(SettingElementButton(this) {
            setting.value = setting.value.javaClass.enumConstants[(setting.value.ordinal + 1) % setting.value.javaClass.enumConstants.size]
        })
        element = DropDown(this, scale * 0.87f * 0.87f)
    }

    override fun change() {
        text = setting.value.name.formatToPretty()
    }

    override fun getSecondaryText() = text

    class DropDown<T: Enum<*>>(val element: EnumElement<T>, scale: Float): DynamicElement() {
        val fontRenderer = RenderHelper.getFontRenderer(scale * 0.87f)

        init {
            val enums = element.setting.value.javaClass.enumConstants
            setHeight { enums.size * scale }
            for((i, value) in enums.withIndex()) {
                pushChild(EnumSelector(this, value).also {
                    it.setY(i * scale)
                    it.setHeight(scale)
                })
            }
        }
    }

    class EnumSelector<T: Enum<*>>(private val dropDown: DropDown<T>, private val value: T): Button(0f, 0f, 0f, 0f, {
        dropDown.element.setting.value = value
    }, Clipping.SCISSOR) {
        private val text = value.name.formatToPretty()
        private val offset = dropDown.fontRenderer.getStringWidth(text) + 2

        override fun getWidth() = getParent()?.getWidth() ?: 0f

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            val width = getWidth()
            val height = getHeight()

            if(dropDown.element.setting.value == value) {
                buffers.triangle.draw(matrixStack) {
                    vertices(
                        0f, 0f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, 0f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, height, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        0f, height, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                    )
                    indices(
                        0, 1, 2,
                        0, 2, 3
                    )
                }
            }

            if(value.ordinal != 0) buffers.lines.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    width, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(0, 1)
            }

            dropDown.fontRenderer.drawString(matrixStack, text, width - offset, 0f, theme.lightground.value)
        }
    }
}
