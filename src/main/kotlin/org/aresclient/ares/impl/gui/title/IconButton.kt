package org.aresclient.ares.impl.gui.title

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.aresclient.ares.api.ngui.NButton
import org.aresclient.ares.api.nrender.HudDrawer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.Theme
import kotlin.math.min
import kotlin.math.pow

class IconButton(private val texture: Identifier, x: Float, y: Float, width: Float, height: Float,
                 action: (NButton) -> Unit): NButton(x, y, width, height, action) {
    private companion object {
        private const val BORDER = 0.03f
        private const val BOUNDS = 0.2f
        private val SHADOW = Color(0f, 0f, 0f, 0.4f)
    }

    override fun drawButton(theme: Theme, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        if(holding) matrixStack.translate(0f, 1f, 0f)

        val size = min(getWidth(), getHeight())
        var offset = BORDER * size

        // draw shadow
        if(!holding) HudDrawer.drawCircle(matrixStack, 1f, 1f, size, SHADOW)

        // draw bordered circle
        HudDrawer.drawCircle(matrixStack, size, theme.primary.value)
        HudDrawer.drawCircle(matrixStack, offset, offset, size - 2 * offset, theme.secondary.value)

        // hover animation
        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
            offset = BORDER + 0.5f * size * (1 - factor)
            HudDrawer.drawCircle(matrixStack, offset, offset, size - 2 * offset, theme.primary.value)
        }

        // draw icon
        offset = BOUNDS * size
        HudDrawer.drawTexture(texture, matrixStack, offset, offset, size - 2 * offset, size - 2 * offset)

        matrixStack.pop()
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val halfW = getWidth() / 2.0
        val halfH = getHeight() / 2.0
        return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
    }
}
