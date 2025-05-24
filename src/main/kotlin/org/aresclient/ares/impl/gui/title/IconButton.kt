package org.aresclient.ares.impl.gui.title

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.aresclient.ares.api.ngui.NButton
import org.aresclient.ares.api.nrender.Drawer
import org.aresclient.ares.impl.util.Theme
import kotlin.math.min
import kotlin.math.pow

class IconButton(private val texture: Identifier, x: Float, y: Float, width: Float, height: Float,
                 action: (NButton) -> Unit): NButton(x, y, width, height, action) {
    private companion object {
        private const val BORDER = 0.03f
        private const val BOUNDS = 0.2f
    }

    // TODO: merge hover and circle into same draw call with dynamic buffers
    override fun drawButton(theme: Theme, drawer: Drawer, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        if(holding) matrixStack.translate(0f, 1f, 0f)

        // TODO:
        // if(!holding) SHADOW.draw(matrixStack)

        val width = getWidth()
        val height = getHeight()
        var offset = BORDER * width

        // draw bordered circle
        drawer.drawEllipse(matrixStack, 0f, 0f, width, height, theme.primary.value)
        drawer.drawEllipse(matrixStack, offset, offset, width - 2 * offset, width - 2 * offset, theme.secondary.value)

        // hover animation
        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
            offset = BORDER + 0.5f * (1 - factor)
            drawer.drawEllipse(matrixStack, offset, offset, width - 2 * offset, height - 2 * offset, theme.primary.value)
        }

        // draw icon
        offset = BOUNDS * width
        drawer.drawTexture(texture, matrixStack, offset, offset, width - 2 * offset, width - 2 * offset)

        matrixStack.pop()
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val halfW = getWidth() / 2.0
        val halfH = getHeight() / 2.0
        return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
    }
}
