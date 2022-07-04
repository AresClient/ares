package org.aresclient.ares.gui.api

import org.aresclient.ares.renderer.*

class Image(private val texture: Texture, x: Float, y: Float, width: Float, height: Float): StaticElement(x, y, width, height) {
    companion object {
        private val BUFFER = Buffer
            .createStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                0f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 1f,
                1f, 1f, 0f, 1f, 1f,
                1f, 0f, 0f, 1f, 0f
            )
            .indices(
                0, 1, 2,
                2, 0, 3
            )
    }

    override fun draw(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        matrixStack.model().scale(getWidth(), getHeight(), 0f)
        texture.bind()
        BUFFER.draw(matrixStack)
        matrixStack.pop()
        super.draw(matrixStack, mouseX, mouseY, delta)
    }
}