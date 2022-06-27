package org.aresclient.ares.gui.api

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.renderer.*
import kotlin.math.min
import kotlin.math.pow

class IconButton(private val texture: Texture, color: Color, outline: Color, x: Float, y: Float, width: Float, height: Float, action: () -> Unit): Button(x, y, width, height, action) {
    private companion object {
        private const val BORDER = 0.03f
        private const val BOUNDS = 0.2f

        private val IMAGE = Buffer.beginStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
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
            .end()

        private val SHADOW = Buffer.beginStatic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR, 4, 6)
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
            .end()
    }

    private val circle = Buffer.beginStatic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR, 8, 12)
        .vertices(
            1f, 1f, 0f, 1f, 1f, outline.red, outline.green, outline.blue, outline.alpha,
            1f, 0f, 0f, 1f, -1f, outline.red, outline.green, outline.blue, outline.alpha,
            0f, 1f, 0f, -1f, 1f, outline.red, outline.green, outline.blue, outline.alpha,
            0f, 0f, 0f, -1f, -1f, outline.red, outline.green, outline.blue, outline.alpha,

            1f - BORDER, 1f - BORDER, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
            1f - BORDER, BORDER, 0f, 1f, -1f, color.red, color.green, color.blue, color.alpha,
            BORDER, 1f - BORDER, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
            BORDER, BORDER, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha
        )
        .indices(
            0, 1, 2,
            1, 2, 3,
            4, 5, 6,
            5, 6, 7
        )
        .end()

    private val hover = Buffer.beginStatic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR, 4, 6)
        .vertices(
            1f - BORDER, 1f - BORDER, 0f, 1f, 1f, outline.red, outline.green, outline.blue, 0.6f,
            1f - BORDER, BORDER, 0f, 1f, -1f, outline.red, outline.green, outline.blue, 0.6f,
            BORDER, 1f - BORDER, 0f, -1f, 1f, outline.red, outline.green, outline.blue, 0.6f,
            BORDER, BORDER, 0f, -1f, -1f, outline.red, outline.green, outline.blue, 0.6f
        )
        .indices(
            0, 1, 2,
            1, 2, 3
        )
        .end()

    override fun draw(matrixStack: MatrixStack) {
        if(holding) matrixStack.model().translate(0f, 1f, 0f)
        matrixStack.model().scale(width, height, 1f)

        if(!holding) SHADOW.draw(matrixStack)

        circle.draw(matrixStack)

        if(hovering || holding) {
            val factor = min((System.currentTimeMillis() - hoverSince) / 200f, 1f)
            val offset = 0.5f * (1 - factor)
            matrixStack.push()
            matrixStack.model().translate(offset, offset, 0f).scale(factor, factor, 1f)
            hover.draw(matrixStack)
            matrixStack.pop()
        }

        texture.bind()
        IMAGE.draw(matrixStack)
    }

    override fun isHovering(mouseX: Float, mouseY: Float): Boolean {
        val halfW = width / 2f
        val halfH = height / 2f
        return (mouseX - x - halfW).pow(2) / halfW.pow(2) + (mouseY - y - halfH).pow(2) / halfH.pow(2) <= 1
    }

    override fun delete() {
        circle.delete()
        hover.delete()
    }
}