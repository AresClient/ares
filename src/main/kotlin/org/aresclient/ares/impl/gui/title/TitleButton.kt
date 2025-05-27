package org.aresclient.ares.impl.gui.title

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.aresclient.ares.api.ngui.NButton
import org.aresclient.ares.api.nrender.HudDrawer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.Theme
import kotlin.math.min

class TitleButton(private val text: Text, x: Float, y: Float, action: (NButton) -> Unit): NButton(x, y, WIDTH, HEIGHT, action) {
    private companion object {
        private const val WIDTH = 150f
        private const val HEIGHT = 22f
        private val SHADOW = Color(0f, 0f, 0f, 0.4f)
    }

    override fun drawButton(theme: Theme, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        if(holding) matrixStack.translate(0f, 1f, 0f)

        // draw shadow
        if(!holding) HudDrawer.drawRect(matrixStack, 1f, 1f, WIDTH, HEIGHT, SHADOW)

        // draw bordered rectangle
        HudDrawer.drawRect(matrixStack, 0f, 0f, WIDTH, HEIGHT, theme.primary.value)
        HudDrawer.drawRect(matrixStack, 1f, 1f, WIDTH - 2f, HEIGHT - 2f, theme.secondary.value)

        // hover animation
        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
            HudDrawer.drawRect(matrixStack, 1f, 1f, (WIDTH - 2) * factor, HEIGHT - 2, theme.primary.value)
        }

        // text
        val textX = WIDTH / 2 - MC.textRenderer.getWidth(text) / 2f
        val textY = HEIGHT / 2 - MC.textRenderer.fontHeight / 2f
        HudDrawer.drawText(text, matrixStack, textX, textY, theme.lightground.value, true)

        matrixStack.pop()
    }
}
