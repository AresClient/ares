package org.aresclient.ares.gui.api

import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer

class TitleScreenButton(private val text: String, x: Float, y: Float, action: () -> Unit): Button(x, y, WIDTH, HEIGHT, action) {
    private companion object {
        private const val WIDTH = 150f
        private const val HEIGHT = 22f
        private val FONT_RENDERER = Renderer.getFontRenderer(14f)
        private val BUFFER = Buffer
            .beginStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 8, 12)
            .vertices(
                WIDTH, HEIGHT, 0f, 1f, 1f, 0.54f, 0.03f, 0.03f, 1f,
                WIDTH, 0f, 0f, 1f, -1f, 0.54f, 0.03f, 0.03f, 1f,
                0f,  HEIGHT, 0f, -1f, 1f, 0.54f, 0.03f, 0.03f, 1f,
                0f, 0f, 0f, -1f, -1f, 0.54f, 0.03f, 0.03f, 1f,

                WIDTH - 1, HEIGHT - 1, 0f, 1f, 1f, 0.09803922f, 0.09803922f, 0.09803922f, 1f,
                WIDTH - 1, 1f, 0f, 1f, -1f, 0.09803922f, 0.09803922f, 0.09803922f, 1f,
                1f,  HEIGHT - 1, 0f, -1f, 1f, 0.09803922f, 0.09803922f, 0.09803922f, 1f,
                1f, 1f, 0f, -1f, -1f, 0.09803922f, 0.09803922f, 0.09803922f, 1f
            )
            .indices(
                0, 1, 2,
                1, 2, 3,
                4, 5, 6,
                5, 6, 7,
            )
            .uniform(Shader.ROUNDED.uniformF2("size").set(WIDTH, HEIGHT))
            .end()
    }

    private val textX = WIDTH / 2 - FONT_RENDERER.getStringWidth(text) / 2f

    override fun draw(matrixStack: MatrixStack) {
        BUFFER.draw(matrixStack)
        FONT_RENDERER.drawString(matrixStack, text, textX, 3f, 1f, 1f, 1f)
    }
}