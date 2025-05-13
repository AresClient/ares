package org.aresclient.ares.impl.util

import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.render.Buffer
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer

object RenderHelper: Wrapper {
    inline fun Buffer.draw(matrixStack: MatrixStack? = null, callback: Buffer.() -> Unit) {
        callback()
        draw(matrixStack ?: MatrixStack.EMPTY)
        reset()
    }

    inline fun scissor(x: Float, y: Float, width: Float, height: Float, callback: () -> Unit) {
        Renderer.scissorBegin(x, y, width, height)
        callback()
        Renderer.scissorEnd()
    }

    inline fun clip(area: () -> Unit, ref: Int = 1, callback: () -> Unit) {
        Renderer.clipBegin(ref)
        area()
        Renderer.clipMask(ref)
        callback()
        Renderer.clipEnd(ref)
    }
}
