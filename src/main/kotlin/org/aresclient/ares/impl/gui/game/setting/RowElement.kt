package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.gui.Button
import org.aresclient.ares.api.gui.DynamicElement
import org.aresclient.ares.api.gui.Element
import org.aresclient.ares.api.gui.ScreenElement
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme

abstract class RowElement(scale: Float, private val start: Float = 3f): DynamicElement(height = { scale }) {
    val fontSize = scale * 13f / 18f

    abstract fun getText(): String

    open fun getTextColor(theme: Theme): Color = theme.lightground.value

    open fun getSecondaryText(): String? = null

    open fun getSecondaryTextColor(theme: Theme): Color = theme.lightground.value

    open fun getTooltip(mouseX: Int, mouseY: Int): String? = null

    open fun shouldRenderTooltip(mouseX: Int, mouseY: Int): Boolean {
        return isMouseOver(mouseX, mouseY)
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        getTooltip(mouseX, mouseY)?.let { (getRootParent() as? ScreenElement)?.setTooltip(it) }

        // outline
        val width = getWidth()
        val height = getHeight()
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, height, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width, height, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                0f, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(
                0, 1,
                0, 2,
                1, 3
            )
        }

        val fontRenderer = theme.font.value.getRenderer()
        val color = getTextColor(theme)
        fontRenderer.drawString(
            matrixStack, getText(), fontSize, start, 1f,
            color.red, color.green, color.blue, color.alpha
        )

        val secondaryText = getSecondaryText()
        if(secondaryText != null) {
            val secondaryTextColor = getSecondaryTextColor(theme)
            fontRenderer.drawString(
                matrixStack, secondaryText, fontSize, getWidth() - fontRenderer.getStringWidth(secondaryText, fontSize) - 2, 1f,
                secondaryTextColor.red, secondaryTextColor.green, secondaryTextColor.blue, secondaryTextColor.alpha
            )
        }


        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    class RowButton(private val element: Element, action: (Button) -> Unit): Button(0f, 0f, 0f, 0f, action, Clipping.SCISSOR) {
        override fun getWidth(): Float = element.getWidth()
        override fun getHeight(): Float = element.getHeight()

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        }
    }

    abstract class SubButton(scale: Float, action: (Button) -> Unit, size: Float = 0.7f, clipping: Clipping = Clipping.STENCIL):
        Button(0f, scale * (1 - size) / 2f, scale * size, scale * size, action, clipping, 2) {
        private val offset = (1f - size) / 2f

        override fun getX(): Float = getParent()?.getWidth()?.let { it - getY() - getWidth()  } ?: 0f
    }

    class SubDeleteButton(scale: Float, action: (Button) -> Unit): SubButton(scale, action, 0.4f, Clipping.NONE) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            val size = getHeight()
            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    size, size, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    0f, size, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    size, 0f, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
                )
                indices(0, 1, 2, 3)
            }
        }
    }

    abstract class SubToggleButton(scale: Float): SubButton(scale, {
        it as SubToggleButton
        it.setState(!it.getState())
    }, 0.5f, Clipping.NONE) {
        abstract fun getState(): Boolean
        abstract fun setState(value: Boolean)

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            val size = getHeight()
            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    size, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    size, size, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, size, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1,
                    1, 2,
                    2, 3,
                    3, 0
                )
            }

            if(getState()) {
                buffers.triangle.draw(matrixStack) {
                    vertices(
                        0f, 0f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        size, 0f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        size, size, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        0f, size, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                    )
                    indices(
                        0, 1, 2,
                        0, 3, 2
                    )
                }
            }
        }
    }
}