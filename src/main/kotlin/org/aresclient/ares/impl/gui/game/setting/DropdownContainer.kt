package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.gui.DynamicElement
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean

abstract class DropdownContainer(protected val scale: Float): RowElement(scale, scale) {
    companion object {
        private const val DROPDOWN_PADDING = 1f
    }

    private var dropdown: DynamicElement? = null
    var open = false

    fun getDropdown(): DynamicElement? = dropdown

    fun setDropdown(element: DynamicElement) {
        element.setX { DROPDOWN_PADDING }
        element.setY { scale }
        element.setWidth { getWidth() - DROPDOWN_PADDING }
        element.setVisible { open }
        this.dropdown?.let { removeChild(it) }
        pushChild(element)
        this.dropdown = element
    }

    override fun getHeight(): Float {
        return if(open) (dropdown?.getHeight() ?: 0f) + scale
        else scale
    }

    override fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        if(isMouseOver(mouseX, mouseY) && !acted.get() && mouseY <= getRenderY() + scale && (mouseButton == 1
                    || (mouseButton == 0 && mouseX <= getRenderX() + scale))) {
            open = !open
            acted.set(true)
        }

        super.click(mouseX, mouseY, mouseButton, acted)
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)

        val fourth = scale / 4f
        val third = scale / 3f
        val half = scale / 2f

        matrixStack.push()
        if(open) matrixStack.model().translate(half, half, 0f).rotateZ((Math.PI / 2).toFloat()).translate(-half, -half, 0f)
        buffers.lines.draw(matrixStack) {
            vertices(
                third, fourth, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                scale - third, half, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                third, scale - fourth, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
            )
            indices(0, 1, 1, 2)
        }
        matrixStack.pop()

        if(open) {
            buffers.triangle.draw(matrixStack) {
                vertices(
                    0f, scale, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    DROPDOWN_PADDING, scale, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, getHeight(), 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    DROPDOWN_PADDING, getHeight(), 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, scale, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    getWidth(), scale, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(0, 1)
            }
        }
    }
}
