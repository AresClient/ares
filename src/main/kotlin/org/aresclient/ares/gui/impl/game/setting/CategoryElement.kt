package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Serializable
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingSubButton
import org.aresclient.ares.gui.impl.game.SettingsContent
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.pow
import kotlin.math.sqrt

class CategoryElement(private val serializable: Serializable, content: SettingsContent):
    SettingElement({ content.open(content.forward(serializable.getName())) }) {
    private val windowButton = WindowButton(serializable, content)

    init {
        pushChild(windowButton)
    }

    override fun getText(): String = serializable.getName()

    private class WindowButton(serializable: Serializable, content: SettingsContent):
        SettingSubButton({ content.getWindow()?.duplicate()?.open(content.forward(serializable.getName())) }) {
        override fun getWidth(): Float = getHeight()

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            val size = getHeight()
            var color = if(isMouseOver(mouseX, mouseY)) theme.secondary else theme.primary

            buffers.ellipse.draw(matrixStack) {
                vertices(
                    size, size, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
                    size, 0f, 0f, 1f, -1f, color.red, color.green, color.blue, color.alpha,
                    0f, size, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
                    0f, 0f, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            val mid = size / 2f
            val padding = size / 3.75f
            val paddingCorner = mid - sqrt(padding * padding / 2f)
            color = theme.lightground

            buffers.lines.draw(matrixStack) {
                vertices(
                    paddingCorner, paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    size - paddingCorner, size - paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    paddingCorner, size - paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    size - paddingCorner, paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 2,   2, 1,
                    1, 3,   3, 0
                )
            }
        }

        override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
            val halfW = getWidth() / 2f
            val halfH = getHeight() / 2f
            return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
        }
    }
}
