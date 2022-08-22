package org.aresclient.ares.gui.api

import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Theme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

abstract class Button(
    x: Float, y: Float, width: Float, height: Float, private var action: (Button) -> Unit = {},
    private val clipping: Clipping = Clipping.STENCIL, private val ref: Int = 1): StaticElement(x, y, width, height) {
    enum class Clipping {
        STENCIL,
        SCISSOR,
        NONE
    }

    companion object {
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

    protected abstract fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int)

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(isMouseOver(mouseX, mouseY)) {
            if(!hovering) {
                hovering = true
                hoverSince = System.currentTimeMillis()
            }
        } else hovering = false

        // draw click circle if holding
        if(clipping != Clipping.NONE && holding) {
            val time = System.currentTimeMillis() - holdSince

            if(clipping == Clipping.STENCIL) {
                Renderer.clip({ draw(theme, buffers, matrixStack, mouseX, mouseY) }, ref) {
                    matrixStack.push()
                    matrixStack.model().translation(holdX, holdY, 0f).scale(min(time / 10f, 4f) + 2f)
                    CIRCLE.draw(matrixStack)
                    matrixStack.pop()
                }
            } else {
                draw(theme, buffers, matrixStack, mouseX, mouseY)
                Renderer.scissor(getRenderX(), getRenderY(), getWidth(), getHeight()) {
                    matrixStack.push()
                    matrixStack.model().translation(holdX, holdY, 0f).scale(min(time / 10f, 4f) + 2f)
                    CIRCLE.draw(matrixStack)
                    matrixStack.pop()
                }
            }
        } else draw(theme, buffers, matrixStack, mouseX, mouseY)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        super.click(mouseX, mouseY, mouseButton, acted)

        if(mouseButton == 0 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            holdSince = System.currentTimeMillis()
            holdX = mouseX.toFloat()
            holdY = mouseY.toFloat()
            holding = true
            acted.set(true)
        }
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0) {
            if(holding && isMouseOver(mouseX, mouseY)) click()
            holding = false
        }
        super.release(mouseX, mouseY, mouseButton)
    }

    fun setAction(action: (Button) -> Unit) {
        this.action = action
    }

    fun click() {
        action(this)
    }
}
