package org.aresclient.ares.gui.api

import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import kotlin.math.min

abstract class Button(var x: Float, var y: Float, var width: Float, var height: Float, private val action: () -> Unit) {
    private companion object {
        private val CIRCLE = Buffer.createStatic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR, 4, 6)
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
    }
    
    protected var hovering = false
    protected var hoverSince = 0L

    protected var holding = false
    protected var holdX = 0f
    protected var holdY = 0f
    protected var holdSince = 0L

    protected abstract fun draw(matrixStack: MatrixStack)

    fun render(matrixStack: MatrixStack, mouseX: Float, mouseY: Float, offsetX: Float = 0f, offsetY: Float = 0f) {
        if(isHovering(mouseX - offsetX, mouseY - offsetY)) {
            if(!hovering) {
                hovering = true
                hoverSince = System.currentTimeMillis()
            }
        } else hovering = false

        // draw button
        matrixStack.push()
        matrixStack.model().translate(x, y, 0f)

        // draw click circle if holding
        if(holding) {
            val time = System.currentTimeMillis() - holdSince

            Renderer.clip({ draw(matrixStack) }) {
                matrixStack.pop()
                matrixStack.push()
                matrixStack.model().translate(holdX, holdY, 0f).scale(min(time / 10f, 4f) + 2f)
                CIRCLE.draw(matrixStack)
            }
        } else draw(matrixStack)

        matrixStack.pop()
    }

    open fun isHovering(mouseX: Float, mouseY: Float) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    fun click(mouseX: Float, mouseY: Float, mouseButton: Int) {
        if(mouseButton == 0 && isHovering(mouseX, mouseY)) {
            holdSince = System.currentTimeMillis()
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

    abstract fun delete()
}