package org.aresclient.ares.impl.util

import org.aresclient.ares.api.render.Buffer
import org.aresclient.ares.api.render.FontRenderer
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import java.awt.Font

object RenderHelper {
    private val fontRenderers = hashMapOf<Int, HashMap<Float, FontRenderer>>()
    private val font = Font.createFont(Font.TRUETYPE_FONT, RenderHelper::class.java.getResourceAsStream("/assets/ares/font/arial.ttf"))

    fun getFontRenderer(size: Float, style: Int) = fontRenderers.getOrPut(style) { hashMapOf() }
        .getOrPut(size) { FontRenderer(font, size, style) }
    fun getFontRenderer(size: Float) = getFontRenderer(size, Font.PLAIN)

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