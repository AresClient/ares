package org.aresclient.ares.gui.impl.game.setting

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Serializable
import org.aresclient.ares.gui.impl.game.CircleSettingSubButton
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingsContent
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.sqrt

class CategoryElement(private val serializable: Serializable, content: SettingsContent):
    SettingElement(serializable.getName(), { content.open(content.forward(serializable.getName())) }) {
    private val windowButton = WindowButton(serializable, content)

    init {
        pushChild(windowButton)
    }

    private class WindowButton(serializable: Serializable, content: SettingsContent):
        CircleSettingSubButton({ content.getWindow()?.duplicate()?.open(content.forward(serializable.getName())) }) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            if(isMouseOver(mouseX, mouseY)) square(buffers, matrixStack, theme.lightground)
            else square(buffers, matrixStack, theme.lightground)
        }

        private fun square(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
            val size = getHeight()
            val mid = size / 2f
            val padding = size / 3.75f
            val paddingCorner = mid - sqrt(padding * padding / 2f)

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
    }
}
