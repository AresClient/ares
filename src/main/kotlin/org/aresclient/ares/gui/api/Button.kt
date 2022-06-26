package org.aresclient.ares.gui.api

import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import org.joml.Vector4f
import kotlin.math.min

abstract class Button(var x: Float, var y: Float, var width: Float, var height: Float, private val action: () -> Unit) {
    private companion object {
        private val CIRCLE = Buffer.beginStatic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR, 4, 6)
            .vertices(
                1f, 1f, 0f,     1f, 1f,      1f, 1f, 1f, 0.3f,
                1f, -1f, 0f,    1f, -1f,     1f, 1f, 1f, 0.3f,
                -1f, 1f, 0f,    -1f, 1f,     1f, 1f, 1f, 0.3f,
                -1f, -1f, 0f,   -1f, -1f,    1f, 1f, 1f, 0.3f
            )
            .indices(
                0, 1, 2,
                1, 2, 3
            )
            .end()
    }

    protected var holding = false
    private var holdX = 0f
    private var holdY = 0f
    private var since = 0L

    protected abstract fun draw(matrixStack: MatrixStack)

    fun render(matrixStack: MatrixStack, offsetX: Float = 0f, offsetY: Float = 0f) {
        // draw button
        matrixStack.push()
        matrixStack.model().translate(x, y, 0f)
        draw(matrixStack)
        matrixStack.pop()

        // draw click circle if holding
        if(holding) {
            val time = System.currentTimeMillis() - since

            Renderer.scissor(x + offsetX, y + offsetY, width, height) {
                matrixStack.push()
                matrixStack.model().translate(holdX, holdY, 0f).scale(min(time / 10f, 4f) + 2f)
                CIRCLE.draw(matrixStack)
                matrixStack.pop()
            }
        }
    }

    fun isHovering(mouseX: Float, mouseY: Float) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    fun click(mouseX: Float, mouseY: Float, mouseButton: Int) {
        if(mouseButton == 0 && isHovering(mouseX, mouseY)) {
            since = System.currentTimeMillis()
            holdX = mouseX
            holdY = mouseY
            holding = true
        }
    }

    fun release(mouseX: Float, mouseY: Float, mouseButton: Int) {
        if(mouseButton == 0) {
            if(holding && isHovering(mouseX, mouseY)) action()
            holding = false
        }
    }
}