package org.aresclient.ares.gui.impl.title

import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import java.awt.Font
import kotlin.math.min

class TitleButton(private val text: String, x: Float, y: Float, action: () -> Unit): Button(x, y, WIDTH, HEIGHT, action) {
    private companion object {
        private const val WIDTH = 150f
        private const val HEIGHT = 22f
        private val FONT_RENDERER = Renderer.getFontRenderer(14f, Font.BOLD)

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

    private val textX = WIDTH / 2 - FONT_RENDERER.getStringWidth(text) / 2f

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        if(!holding) SHADOW.draw(matrixStack)
        else matrixStack.model().translate(0f, 1f, 0f)

        buffers.uniforms.roundedRadius.set(0.08f)
        buffers.uniforms.roundedSize.set(WIDTH, HEIGHT)
        buffers.rounded.draw(matrixStack) {
            vertices(
                // outline
                WIDTH, HEIGHT, 0f, 1f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                WIDTH, 0f, 0f, 1f, -1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                0f,  HEIGHT, 0f, -1f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                0f, 0f, 0f, -1f, -1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,

                // inner
                WIDTH - 1, HEIGHT - 1, 0f, 1f, 1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                WIDTH - 1, 1f, 0f, 1f, -1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                1f,  HEIGHT - 1, 0f, -1f, 1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                1f, 1f, 0f, -1f, -1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
            )
            indices(
                0, 1, 2,
                1, 2, 3,
                4, 5, 6,
                5, 6, 7
            )
        }

        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)

            buffers.uniforms.roundedSize.set(WIDTH / factor, HEIGHT)
            buffers.rounded.draw(matrixStack) {
                vertices(
                    (WIDTH - 1) * factor, HEIGHT - 1, 0f, 1f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, 0.6f,
                    (WIDTH - 1) * factor, 1f, 0f, 1f, -1f, theme.primary.red, theme.primary.green, theme.primary.blue, 0.6f,
                    1f,  HEIGHT - 1, 0f, -1f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, 0.6f,
                    1f, 1f, 0f, -1f, -1f, theme.primary.red, theme.primary.green, theme.primary.blue, 0.6f
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }
        }

        FONT_RENDERER.drawString(matrixStack, text, textX, 3f, 1f, 1f, 1f, 1f)
    }
}
