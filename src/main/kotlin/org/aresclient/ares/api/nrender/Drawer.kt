package org.aresclient.ares.api.nrender

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.mixin.accessors.AccessDrawContext

class Drawer(val context: DrawContext) {
    companion object {
        private val indexedVertexConsumers = IndexedVertexConsumerProvider()
    }

    private val vertexConsumers = (context as AccessDrawContext).vertexConsumers

    fun drawRect(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayer.getGui())
        buffer.vertex(matrix4f, x,         y,          0.0f).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0.0f).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0.0f).color(color)
        buffer.vertex(matrix4f, x + width, y,          0.0f).color(color)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Color) {
        val buffer = vertexConsumers.getBuffer(RenderLayer.getGui())
        buffer.vertex(x,         y,          0.0f).color(color)
        buffer.vertex(x,         y + height, 0.0f).color(color)
        buffer.vertex(x + width, y + height, 0.0f).color(color)
        buffer.vertex(x + width, y,          0.0f).color(color)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float) {
        drawTexture(texture, matrixStack, x, y, width, height, 0f, 0f, 1f, 1f)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        drawTexture(texture, matrixStack, x, y, width, height, 0f, 0f, 1f, 1f, color)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, u1: Float, u2: Float, v1: Float, v2: Float) {
        drawTexture(texture, matrixStack, x, y, width, height, u1, v1, u2, v2, Color.WHITE)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, u1: Float, u2: Float, v1: Float, v2: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayers.texture.apply(texture))
        buffer.vertex(matrix4f, x,         y,          0.0f).texture(u1, v1).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0.0f).texture(u1, v2).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0.0f).texture(u2, v2).color(color)
        buffer.vertex(matrix4f, x + width, y,          0.0f).texture(u2, v1).color(color)
    }

    fun drawTexture(texture: Identifier, x: Float, y: Float, width: Float, height: Float) {
        drawTexture(texture, x, y, width, height, 0f, 0f, 1f, 1f)
    }

    fun drawTexture(texture: Identifier, x: Float, y: Float, width: Float, height: Float, color: Color) {
        drawTexture(texture, x, y, width, height, 0f, 0f, 1f, 1f, color)
    }

    fun drawTexture(texture: Identifier, x: Float, y: Float, width: Float, height: Float, u1: Float, v1: Float, u2: Float, v2: Float) {
        drawTexture(texture, x, y, width, height, u1, v1, u2, v2, Color.WHITE)
    }

    fun drawTexture(texture: Identifier, x: Float, y: Float, width: Float, height: Float, u1: Float, v1: Float, u2: Float, v2: Float, color: Color) {
        val buffer = vertexConsumers.getBuffer(RenderLayers.texture.apply(texture))
        buffer.vertex(x,         y,          0.0f).texture(u1, v1).color(color)
        buffer.vertex(x,         y + height, 0.0f).texture(u1, v2).color(color)
        buffer.vertex(x + width, y + height, 0.0f).texture(u2, v2).color(color)
        buffer.vertex(x + width, y,          0.0f).texture(u2, v1).color(color)
    }

    fun drawEllipse(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayers.ellipse)
        buffer.vertex(matrix4f, x,         y,          0f).texture(-1f, -1f).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).texture(-1f, 1f).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).texture(1f, 1f).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).texture(1f, -1f).color(color)
    }

    fun drawEllipse(x: Float, y: Float, width: Float, height: Float, color: Color) {
        val buffer = vertexConsumers.getBuffer(RenderLayers.ellipse)
        buffer.vertex(x,         y,          0f).texture(-1f, -1f).color(color)
        buffer.vertex(x,         y + height, 0f).texture(-1f, 1f).color(color)
        buffer.vertex(x + width, y + height, 0f).texture(1f, 1f).color(color)
        buffer.vertex(x + width, y,          0f).texture(1f, -1f).color(color)
    }

    fun draw() {
        vertexConsumers.draw()
        indexedVertexConsumers.draw()
    }

    private fun VertexConsumer.color(color: Color): VertexConsumer = color(color.red, color.green, color.blue, color.alpha)
}
