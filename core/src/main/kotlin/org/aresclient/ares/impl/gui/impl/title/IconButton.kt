package org.aresclient.ares.impl.gui.impl.title

import org.aresclient.ares.api.render.*
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import kotlin.math.min
import kotlin.math.pow

class IconButton(private val texture: Texture, x: Float, y: Float, width: Float, height: Float,
                 action: (Button) -> Unit): Button(x, y, width, height, action) {
    private companion object {
        private const val BORDER = 0.03f
        private const val BOUNDS = 0.2f

        private val IMAGE = Buffer.createStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                1f - BOUNDS, 1f - BOUNDS, 0f, 1f, 1f,
                1f - BOUNDS, BOUNDS, 0f, 1f, 0f,
                BOUNDS, 1f - BOUNDS, 0f, 0f, 1f,
                BOUNDS, BOUNDS, 0f, 0f, 0f
            )
            .indices(
                0, 1, 2,
                1, 2, 3
            )

        private val SHADOW = Buffer.createStatic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR, 4, 6)
            .vertices(
                BORDER + 1, BORDER + 1, 0f, 1f, 1f, 0f, 0f, 0f, 0.4f,
                BORDER + 1, 0f, 0f, 1f, -1f, 0f, 0f, 0f, 0.4f,
                -BORDER, BORDER + 1, 0f, -1f, 1f, 0f, 0f, 0f, 0.4f,
                -BORDER, 0f, 0f, -1f, -1f, 0f, 0f, 0f, 0.4f
            )
            .indices(
                0, 1, 2,
                1, 2, 3
            )
    }

    // TODO: merge hover and circle into same draw call with dynamic buffers
    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        if(holding) matrixStack.model().translate(0f, 1f, 0f)
        matrixStack.model().scale(getWidth(), getHeight(), 1f)

        if(!holding) SHADOW.draw(matrixStack)

        buffers.ellipse.draw(matrixStack) {
            vertices(
                1f, 1f, 0f, 1f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                1f, 0f, 0f, 1f, -1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                0f, 1f, 0f, -1f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                0f, 0f, 0f, -1f, -1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,

                1f - BORDER, 1f - BORDER, 0f, 1f, 1f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                1f - BORDER, BORDER, 0f, 1f, -1f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                BORDER, 1f - BORDER, 0f, -1f, 1f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                BORDER, BORDER, 0f, -1f, -1f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3,
                4, 5, 6,
                5, 6, 7
            )

            if(hovering || holding) {
                val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
                val offset = 0.5f * (1 - factor)

                vertices(
                    1f - BORDER - offset, 1f - BORDER - offset, 0f, 1f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, 0.6f,
                    1f - BORDER - offset, BORDER + offset, 0f, 1f, -1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, 0.6f,
                    BORDER + offset, 1f - BORDER - offset, 0f, -1f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, 0.6f,
                    BORDER + offset, BORDER + offset, 0f, -1f, -1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, 0.6f
                )
                indices(
                    8, 9, 10,
                    9, 10, 11
                )
            }
        }

        texture.bind()
        IMAGE.draw(matrixStack)
    }

    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val halfW = getWidth() / 2f
        val halfH = getHeight() / 2f
        return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
    }
}
