package org.aresclient.ares.api.nrender.hud

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.AresRenderLayers
import org.aresclient.ares.api.util.Color
import org.joml.Vector3f

object HudDrawer: Wrapper {
    private val vertexConsumers = MC.bufferBuilders.entityVertexConsumers

    fun drawTextCentered(font: NFont, text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Color, size: Float = 11f, shadow: Boolean = false): Int {
        val cx = x - font.getWidth(text, size) / 2f
        val cy = y - font.getHeight(size) / 2f
        return drawText(font, text, matrixStack, cx, cy, color, size, shadow)
    }

    fun drawText(font: NFont, text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Color, size: Float = 11f, shadow: Boolean = false): Int {
        return drawText(font, text, matrixStack, x, y, color.rgba, size, shadow)
    }

    private fun drawText(font: NFont, text: Text, matrixStack: MatrixStack, x: Float, y: Float, color: Int, size: Float, shadow: Boolean): Int {
        val scale = font.getScale(size)
        matrixStack.push()
        matrixStack.scale(scale, scale, 1f)
        val i = font.textRenderer.draw(
            text, x / scale, y / scale, color, shadow, matrixStack.peek().positionMatrix,
            vertexConsumers, TextRenderer.TextLayerType.NORMAL, Color.BLACK.rgba, 15728880
        )
        matrixStack.pop()
        return i
    }

    fun drawRect(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(RenderLayer.getGui())
        buffer.vertex(matrix4f, x,         y,          0f).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).color(color)
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
        val buffer = vertexConsumers.getBuffer(AresRenderLayers.QUAD_TEXTURE.apply(texture))
        buffer.vertex(matrix4f, x,         y,          0f).texture(u1, v1).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).texture(u1, v2).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).texture(u2, v2).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).texture(u2, v1).color(color)
    }

    fun drawCircle(matrixStack: MatrixStack, x: Float, y: Float, size: Float, color: Color) {
        drawEllipse(matrixStack, x, y, size, size, color)
    }

    fun drawEllipse(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(AresRenderLayers.QUAD_ELLIPSE)
        buffer.vertex(matrix4f, x,         y,          0f).texture(-1f, -1f).color(color)
        buffer.vertex(matrix4f, x,         y + height, 0f).texture(-1f, 1f).color(color)
        buffer.vertex(matrix4f, x + width, y + height, 0f).texture(1f, 1f).color(color)
        buffer.vertex(matrix4f, x + width, y,          0f).texture(1f, -1f).color(color)
    }

    fun drawRoundedRect(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, roundness: Float, color: Color) {
        if(width == height) drawRoundedSquare(matrixStack, x, y, width, roundness, color)
        else if(width > height) {
            val offset = height / 2
            drawRoundedVerticalHalf(matrixStack, x, y, offset, height, roundness, color, 0)
            drawRect(matrixStack, x + offset, y, width - height, height, color)
            drawRoundedVerticalHalf(matrixStack, x + width - offset, y, offset, height, roundness, color, 1)
        } else { // height > width
            val offset = width / 2
            drawRoundedHorizontalHalf(matrixStack, x, y, width, offset, roundness, color, 0)
            drawRect(matrixStack, x, y + offset, width, height - width, color)
            drawRoundedHorizontalHalf(matrixStack, x, y + height - offset, width, offset, roundness, color, 1)
        }
    }

    // side = 0 = left, side = 1 = right
    private fun drawRoundedVerticalHalf(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, roundness: Float, color: Color, side: Int) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(AresRenderLayers.QUAD_ROUNDED)
        val begin = (-1 + side).toFloat()
        val end = side.toFloat()
        buffer.vertex(matrix4f, x,         y,          0f).color(color).normal(begin, -1f, roundness)
        buffer.vertex(matrix4f, x,         y + height,   0f).color(color).normal(begin, 1f, roundness)
        buffer.vertex(matrix4f, x + width, y + height,   0f).color(color).normal(end, 1f, roundness)
        buffer.vertex(matrix4f, x + width, y,          0f).color(color).normal(end, -1f, roundness)
    }

    // side = 0 = top, side = 1 = bottom
    private fun drawRoundedHorizontalHalf(matrixStack: MatrixStack, x: Float, y: Float, width: Float, height: Float, roundness: Float, color: Color, side: Int) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(AresRenderLayers.QUAD_ROUNDED)
        val begin = (-1 + side).toFloat()
        val end = side.toFloat()
        buffer.vertex(matrix4f, x,         y,          0f).color(color).normal(-1f, begin, roundness)
        buffer.vertex(matrix4f, x,         y + height,   0f).color(color).normal(-1f, end, roundness)
        buffer.vertex(matrix4f, x + width, y + height,   0f).color(color).normal(1f, end, roundness)
        buffer.vertex(matrix4f, x + width, y,          0f).color(color).normal(1f, begin, roundness)
    }

    fun drawRoundedSquare(matrixStack: MatrixStack, x: Float, y: Float, size: Float, roundness: Float, color: Color) {
        val matrix4f = matrixStack.peek().positionMatrix
        val buffer = vertexConsumers.getBuffer(AresRenderLayers.QUAD_ROUNDED)
        buffer.vertex(matrix4f, x,         y,          0f).color(color).normal(-1f, -1f, roundness)
        buffer.vertex(matrix4f, x,         y + size, 0f).color(color).normal(-1f, 1f, roundness)
        buffer.vertex(matrix4f, x + size, y + size, 0f).color(color).normal(1f, 1f, roundness)
        buffer.vertex(matrix4f, x + size, y,          0f).color(color).normal(1f, -1f, roundness)
    }

    fun draw() {
        vertexConsumers.draw()
    }

    private fun VertexConsumer.color(color: Color): VertexConsumer = color(color.red, color.green, color.blue, color.alpha)
}
