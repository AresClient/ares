package org.aresclient.ares.gui.api

import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import java.awt.Font
import kotlin.math.min

class TitleScreenButton(private val text: String, x: Float, y: Float, action: () -> Unit): Button(x, y, WIDTH, HEIGHT, action) {
    private companion object {
        private const val WIDTH = 150f
        private const val HEIGHT = 22f
        private val RADIUS = Shader.ROUNDED.uniformF1("radius").set(0.08f)
        private val SIZE = Shader.ROUNDED.uniformF2("size").set(WIDTH, HEIGHT)
        private val FONT_RENDERER = Renderer.getFontRenderer(14f, Font.BOLD)

        private val BUFFER = Buffer
            .beginStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 8, 12)
            .vertices(
                // outline
                WIDTH, HEIGHT, 0f, 1f, 1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
                WIDTH, 0f, 0f, 1f, -1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
                0f,  HEIGHT, 0f, -1f, 1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
                0f, 0f, 0f, -1f, -1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,

                // inner
                WIDTH - 1, HEIGHT - 1, 0f, 1f, 1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                WIDTH - 1, 1f, 0f, 1f, -1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                1f,  HEIGHT - 1, 0f, -1f, 1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                1f, 1f, 0f, -1f, -1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            )
            .indices(
                0, 1, 2,
                1, 2, 3,
                4, 5, 6,
                5, 6, 7
            )
            .uniform(RADIUS)
            .uniform(SIZE)
            .end()

        private val SHADOW = Buffer
            .beginStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 4, 6)
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
            .uniform(RADIUS)
            .uniform(SIZE)
            .end()

        private val HOVER = Buffer
            .beginStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 4, 6)
            .vertices(
                WIDTH - 1, HEIGHT - 1, 0f, 1f, 1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, 0.6f,
                WIDTH - 1, 1f, 0f, 1f, -1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, 0.6f,
                1f,  HEIGHT - 1, 0f, -1f, 1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, 0.6f,
                1f, 1f, 0f, -1f, -1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, 0.6f
            )
            .indices(
                0, 1, 2,
                1, 2, 3
            )
            .uniform(RADIUS)
            .uniform(SIZE)
            .end()
    }

    private val textX = WIDTH / 2 - FONT_RENDERER.getStringWidth(text) / 2f

    override fun draw(matrixStack: MatrixStack) {
        if(!holding) SHADOW.draw(matrixStack)
        else matrixStack.model().translate(0f, 1f, 0f)

        BUFFER.draw(matrixStack)

        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
            matrixStack.push()
            matrixStack.model().scale(factor, 1f, 1f)
            SIZE.set(WIDTH / factor, HEIGHT)
            HOVER.draw(matrixStack)
            SIZE.set(WIDTH, HEIGHT)
            matrixStack.pop()
        }

        FONT_RENDERER.drawString(matrixStack, text, textX, 3f, 1f, 1f, 1f)
    }

    override fun delete() {
    }
}