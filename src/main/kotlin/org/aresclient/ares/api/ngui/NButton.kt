package org.aresclient.ares.api.ngui

import net.minecraft.client.util.math.MatrixStack
import org.aresclient.ares.api.nrender.hud.HudDrawer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

abstract class NButton(x: Float, y: Float, width: Float, height: Float, private var action: (NButton) -> Unit = {}): NStaticElement(x, y, width, height) {
    // TODO: click circle clipping?
    protected var hovering = false
    protected var hoverSince = 0L

    protected var holding = false
    private var holdX = 0f
    private var holdY = 0f
    private var holdSince = 0L

    protected abstract fun drawButton(theme: Theme, drawer: HudDrawer, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)

    override fun draw(theme: Theme, drawer: HudDrawer, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(isMouseOver(mouseX, mouseY)) {
            if(!hovering) {
                hovering = true
                hoverSince = System.currentTimeMillis()
            }
        } else hovering = false

        drawButton(theme, drawer, matrixStack, mouseX, mouseY, delta)

        // draw click circle if holding
        if(holding) {
            val time = System.currentTimeMillis() - holdSince
            val scale = min(time / 20f, 2f) + 1f
            drawClickCircle(drawer, matrixStack, holdX, holdY, scale * 2f)
        }

        super.draw(theme, drawer, matrixStack, mouseX, mouseY, delta)
    }

    private fun drawClickCircle(drawer: HudDrawer, matrixStack: MatrixStack, x: Float, y: Float, scale: Float) {
        matrixStack.push()
        matrixStack.loadIdentity() // probably not optimal
        matrixStack.translate(0f, 0f, 100f)
        drawer.drawEllipse(matrixStack, x - scale, y - scale, scale * 2, scale * 2, Color.WHITE.deriveAlpha(0.3f))
        matrixStack.pop()
    }

    override fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        super.click(mouseX, mouseY, mouseButton, acted)

        if(mouseButton == 0 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            holdSince = System.currentTimeMillis()
            holdX = mouseX.toFloat()
            holdY = mouseY.toFloat()
            holding = true
            acted.set(true)
        }
    }

    override fun release(mouseX: Double, mouseY: Double, mouseButton: Int) {
        if(mouseButton == 0) {
            if(holding && isMouseOver(mouseX, mouseY)) click()
            holding = false
        }
        super.release(mouseX, mouseY, mouseButton)
    }

    fun setAction(action: (NButton) -> Unit) {
        this.action = action
    }

    fun click() {
        action(this)
    }
}
