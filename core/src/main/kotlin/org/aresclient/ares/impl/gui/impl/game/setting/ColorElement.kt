package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.gui.api.DynamicElement
import org.aresclient.ares.impl.gui.api.StaticElement
import org.aresclient.ares.impl.gui.impl.game.*
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean

class ColorElement(private val content: WindowContent, setting: Setting.Color, scale: Float):
    DropDownSettingElement<Setting.Color>(setting, scale) {
    private val button = ColorSelectButton(this, scale)

    init {
        pushChild(SettingElementButton(this) {
            content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsContent::class.java
            }
        })
        pushChild(button)
        element = Selector(setting, scale * 0.87f * 0.87f)
    }

    class Selector(val setting: Setting.Color, scale: Float): DynamicElement() {
        private val selector = ColorSelector(this, if(setting.isRainbow) ColorSelector.Type.RNBW else ColorSelector.Type.RGB, scale)
        private val rnbw = RNBWColorSelectElement(this, scale).setVisible { selector.value == ColorSelector.Type.RNBW }
        private val rgb = RGBColorSelectElement(this, scale).setVisible { selector.value == ColorSelector.Type.RGB }
        val fontRenderer = RenderHelper.getFontRenderer(scale * 0.87f)

        init {
            pushChild(selector)
            setHeight { scale + (getChildren().firstOrNull { it.isVisible() && it != selector }?.getHeight() ?: 0f) }
            arrayOf(rgb, rnbw).forEach {
                it.setY { scale }
                it.setWidth { getWidth() }
                pushChild(it)
            }
        }
    }

    private class RGBColorSelectElement(val element: Selector, private val scale: Float): DynamicElement(height = { scale * 4 }) {
        init {
            arrayOf(
                Slider(element, "Red", 0f, 1f, { element.setting.value.red }) { element.setting.value.red = it },
                Slider(element, "Green", 0f, 1f, { element.setting.value.green }) { element.setting.value.green = it },
                Slider(element, "Blue", 0f, 1f, { element.setting.value.blue }) { element.setting.value.blue = it },
                Slider(element, "Alpha", 0f, 1f, { element.setting.value.alpha }) { element.setting.value.alpha = it }
            ).forEachIndexed { ind, it ->
                it.setY(ind * scale)
                it.setHeight(scale)
                pushChild(it)
            }
        }

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
            val width = getWidth()
            buffers.lines.draw(matrixStack) {
                for(i in 1..3) {
                    vertices(
                        0f, i * scale, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, i * scale, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                    )
                }
                indices(0, 1, 2, 3, 4, 5)
            }

            super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
        }
    }

    private class RNBWColorSelectElement(val element: Selector, scale: Float): DynamicElement(height = { scale }) {
        init {
            Slider(element, "Alpha", 0f, 1f, { element.setting.value.alpha }) { element.setting.setAlpha(it) }.also {
                it.setHeight(scale)
                pushChild(it)
            }
        }
    }

    private class ColorSelectButton(private val colorElement: ColorElement, scale: Float): SettingSubButton(scale, {
         colorElement.open = !colorElement.open
    }, 0.5f) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            val size = getHeight()
            val value = colorElement.setting.value

            buffers.triangle.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, value.red, value.green, value.blue, value.alpha,
                    size, 0f, 0f, value.red, value.green, value.blue, value.alpha,
                    size, size, 0f, value.red, value.green, value.blue, value.alpha,
                    0f, size, 0f, value.red, value.green, value.blue, value.alpha
                )
                indices(
                    0, 1, 2,
                    0, 3, 2
                )
            }

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    size, 0f, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    size, size, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    0f, size, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
                )
                indices(
                    0, 1,
                    1, 2,
                    2, 3,
                    3, 0
                )
            }
        }
    }

    private class ColorSelector(private val element: Selector, var value: ColorSelector.Type, scale: Float): StaticElement(height = scale) {
        enum class Type {
            RGB,
            RNBW
        }

        init {
            Type.values().forEach { pushChild(ColorSelectionTypeButton(this, it)) }
        }

        override fun getWidth() = getParent()?.getWidth() ?: 0f

        private class ColorSelectionTypeButton(private val selector: ColorSelector, private val value: Type):
        Button(0f, 0f, 0f, 0f, {
            selector.value = value
            selector.element.setting.isRainbow = value == Type.RNBW
        }, clipping = Clipping.SCISSOR) {
            override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
                val width = getWidth()
                val height = getHeight()

                buffers.lines.draw(matrixStack) {
                    vertices(
                        0f, 0f, 0f, 2f,     theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        0f, height, 0f, 2f,     theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, height, 0f, 2f,     theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, 0f, 0f, 2f,     theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                    )
                    indices(
                        0, 1,
                        1, 2,
                        2, 3,
                        3, 0
                    )
                }

                if(selector.value == value) buffers.triangle.draw(matrixStack) {
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

                val textWidth = selector.element.fontRenderer.getStringWidth(value.name)
                selector.element.fontRenderer.drawString(matrixStack, value.name, width / 2f - textWidth / 2f, height / 2f - selector.element.fontRenderer.charHeight / 2f, theme.lightground.value)
            }

            override fun getX() = value.ordinal * getWidth()
            override fun getWidth() = (getParent()?.getWidth() ?: 0f) / selector.value.javaClass.enumConstants.size
            override fun getHeight() = getParent()?.getHeight() ?: 0f
        }
    }

    private class Slider(private val element: Selector, private val name: String, private val min: Float, private val max: Float,
                         private val get: () -> Float, private val set: (Float) -> Unit): StaticElement() {
        private var holding = false

        override fun getWidth() = getParent()?.getWidth() ?: 0f

        override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
            super.click(mouseX, mouseY, mouseButton, acted)

            if(!acted.get() && !holding && isMouseOver(mouseX, mouseY) && mouseButton == 0) {
                holding = true
                acted.set(true)
            }
        }

        override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
            super.release(mouseX, mouseY, mouseButton)
            if(mouseButton == 0) holding = false
        }

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
            if(holding) ((((mouseX - getRenderX()) / getWidth()).coerceIn(0f, 1f) * (max - min)) + min).also {
                set(it)
            }

            val value = get()
            val height = getHeight()
            val offset = (value - min) / (max - min) * getWidth()
            buffers.triangle.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f,          theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, height, 0f,      theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    offset, 0f, 0f,      theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    offset, height, 0f,  theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1, 3,
                    0, 2, 3
                )
            }

            val textY = getHeight() / 2f - element.fontRenderer.charHeight / 2f
            element.fontRenderer.drawString(
                matrixStack, name, 3f, textY,
                theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
            )

            val text = (value * 255).toInt().toString()
            element.fontRenderer.drawString(
                matrixStack, text, getWidth() - element.fontRenderer.getStringWidth(text) - 2f, textY,
                theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
            )

            super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
        }
    }
}
