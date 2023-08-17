package org.aresclient.ares.impl.gui.api

import org.aresclient.ares.api.render.*
import org.aresclient.ares.impl.util.Theme

class Image(private var texture: Texture, x: Float, y: Float, width: Float, height: Float): StaticElement(x, y, width, height) {
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

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        matrixStack.model().scale(getWidth(), getHeight(), 0f)
        texture.bind()
        BUFFER.draw(matrixStack)
        matrixStack.pop()
        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    fun setTexture(texture: Texture) {
        this.texture = texture
    }
}
