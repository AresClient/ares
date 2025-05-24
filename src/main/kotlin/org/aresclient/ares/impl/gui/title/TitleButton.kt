package org.aresclient.ares.impl.gui.title

import net.minecraft.client.util.math.MatrixStack
import org.aresclient.ares.api.ngui.NButton
import org.aresclient.ares.api.nrender.Drawer
import org.aresclient.ares.api.render.*
import org.aresclient.ares.impl.util.Theme
import kotlin.math.min

class TitleButton(private val text: String, x: Float, y: Float, action: (NButton) -> Unit): NButton(x, y, WIDTH, HEIGHT, action) {
    private companion object {
        private const val WIDTH = 150f
        private const val HEIGHT = 22f

        private val SHADOW = Buffer
            .createStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 4, 6)
            .vertices(
                WIDTH + 1, HEIGHT + 1, 0f, 1f, 1f, 0f, 0f, 0f, 0.4f,
                WIDTH + 1, 1f, 0f, 1f, -1f, 0f, 0f, 0f, 0.4f,
                -1f,  HEIGHT + 1, 0f, -1f, 1f, 0f, 0f, 0f, 0.4f,
                -1f, 1f, 0f, -1f, -1f, 0f, 0f, 0f, 0.4f
            )
            .indices(
                0, 1, 2,
                1, 2, 3
            )
            .uniform(Shader.ROUNDED.uniformF2("size").set(WIDTH, HEIGHT))
    }

    override fun drawButton(theme: Theme, drawer: Drawer, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        if(holding) matrixStack.translate(0f, 1f, 0f)

        // TODO:
        // if(!holding) SHADOW.draw(matrixStack)

        drawer.drawRect(matrixStack, 0f, 0f, WIDTH, HEIGHT, theme.primary.value)
        drawer.drawRect(matrixStack, 1f, 1f, WIDTH - 2f, HEIGHT - 2f, theme.secondary.value)

        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
            drawer.drawRect(matrixStack, 1f, 1f, (WIDTH - 2) * factor, (HEIGHT - 2) * factor, theme.secondary.value)
        }

        val textRenderer = MC.textRenderer
        val textX = WIDTH / 2 - textRenderer.getWidth(text) / 2f
        val textY = HEIGHT / 2 - textRenderer.fontHeight / 2f

        // TODO: fix text rendering hack
        drawer.context.matrices.push()
        drawer.context.matrices.translate(getRenderX(), getRenderY(), 0f)
        if(holding) drawer.context.matrices.translate(0f, 1f, 0f)
        drawer.context.drawText(MC.textRenderer, text, textX.toInt(), textY.toInt(), theme.lightground.value.rgba, false)
        drawer.context.matrices.pop()

        matrixStack.pop()
    }
}
