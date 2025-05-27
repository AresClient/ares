package org.aresclient.ares.api.nrender

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.util.Color

object HudDrawer: Wrapper {
    private val vertexConsumers = MC.bufferBuilders.entityVertexConsumers

    fun drawTextWithShadow(text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Color): Int {
        return drawText(text, matrixStack, x, y, color.rgba, false)
    }

    fun drawText(text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Color): Int {
        return drawText(text, matrixStack, x, y, color.rgba, false)
    }

    fun drawText(text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Color, shadow: Boolean): Int {
        return drawText(text, matrixStack, x, y, color.rgba, shadow)
    }

    private fun drawText(text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Int, shadow: Boolean): Int {
        return MC.textRenderer.draw(
            text, x, y, color, shadow, matrixStack.peek().positionMatrix,
            vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880
        )
    }

    fun drawRect(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayer.getGui())
        buffer.vertex(matrix4f, x,         y,          0f).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).color(color)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, size: Float) {
        drawTexture(texture, matrixStack, size, size)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, size: Float, color: Color) {
        drawTexture(texture, matrixStack, size, size, color)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, width: Float, height: Float) {
        drawTexture(texture, matrixStack, width, height, Color.WHITE)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, width: Float, height: Float, color: Color) {
        drawTexture(texture, matrixStack, 0f, 0f, width, height, 0f, 0f, 1f, 1f, color)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float) {
        drawTexture(texture, matrixStack, x, y, width, height, 0f, 0f, 1f, 1f)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        drawTexture(texture, matrixStack, x, y, width, height, 0f, 0f, 1f, 1f, color)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, u1: Float, v1: Float, u2: Float, v2: Float) {
        drawTexture(texture, matrixStack, x, y, width, height, u1, v1, u2, v2, Color.WHITE)
    }

    fun drawTexture(texture: Identifier, matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, u1: Float, v1: Float, u2: Float, v2: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayers.Hud.quad_texture.apply(texture))
        buffer.vertex(matrix4f, x,         y,          0f).texture(u1, v1).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).texture(u1, v2).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).texture(u2, v2).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).texture(u2, v1).color(color)
    }

    fun drawCircle(matrixStack: MatrixStack, size: Float, color: Color) {
        drawCircle(matrixStack, 0f, 0f, size, color)
    }

    fun drawEllipse(matrixStack: MatrixStack, width: Float, height: Float, color: Color) {
        drawEllipse(matrixStack, 0f, 0f, width, height, color)
    }

    fun drawCircle(matrixStack: MatrixStack, x: Float, y: Float, size: Float, color: Color) {
        drawEllipse(matrixStack, x, y, size, size, color)
    }

    fun drawEllipse(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayers.Hud.quad_ellipse)
        buffer.vertex(matrix4f, x,         y,          0f).texture(-1f, -1f).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).texture(-1f, 1f).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).texture(1f, 1f).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).texture(1f, -1f).color(color)
    }

    fun draw() {
        vertexConsumers.draw()
    }

    private fun VertexConsumer.color(color: Color): VertexConsumer = color(color.red, color.green, color.blue, color.alpha)
}
