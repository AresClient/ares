package org.aresclient.ares.gui.api

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.renderer.FontRenderer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.atomic.AtomicBoolean

open class TextBox(x: Float, y: Float, width: Float, fontSize: Float, private val minLines: Int, vertPadFactor: Float = 0.2f, horzPadFactor: Float = 0.5f):
    StaticElement(x, y, width, 0f) {

    private val fontRenderer = Renderer.getFontRenderer(fontSize)
    private val vertPadding = fontRenderer.charHeight * vertPadFactor
    private val horzPadding = fontRenderer.charHeight * horzPadFactor

    private var text = ""
    private var lines = minLines
    private var cursor = 0
    private var focused = false

    override fun getHeight(): Float = (fontRenderer.charHeight + vertPadding) * lines + vertPadding

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val width = getWidth()
        val height = getHeight()

        buffers.triangle.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                width, 0f, 0f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                0f, height, 0f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                width, height, 0f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        buffers.lines.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                width, 0f, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                width, height, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                0f, height, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(
                0, 1,
                1, 2,
                2, 3,
                3, 0
            )
        }

        lines = max(drawText(buffers, matrixStack, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha), minLines)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        super.click(mouseX, mouseY, mouseButton, acted)

        if(!acted.get() && mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
            var i = 0
            val unused = FloatArray(4)
            val rx = getRenderX()
            val ry = getRenderX()

            runText(unused) { c, cx, cy ->
                val cw = fontRenderer.getCharWidth(c)
                if(mouseY >= cy + ry && mouseY <= cy + ry + fontRenderer.charHeight && mouseX >= cx + rx && mouseX <= cx + rx + cw) {
                    cursor = if(mouseX < cx + rx + cw / 2f) i else i + 1
                    return@runText true
                }

                i++
                false
            }

            focused = true
            acted.set(true)
        } else focused = false
    }

    override fun type(typedChar: Char?, keyCode: Int) {
        super.type(typedChar, keyCode)
        if(!focused) return

        if(typedChar == null) when(keyCode) {
            Keys.LEFT -> cursor = max(0, cursor - 1)
            Keys.RIGHT -> cursor = min(text.length, cursor + 1)
            Keys.BACKSPACE -> if(cursor > 0) text = text.removeRange(cursor - 1, cursor--)
            Keys.ENTER -> append('\n')
            Keys.ESCAPE -> focused = false
        } else append(typedChar)
    }

    private fun append(char: Char) {
        val sb = StringBuilder()
        sb.append(text.substring(0, cursor))
        sb.append(char)
        sb.append(text.substring(cursor++))
        text = sb.toString()
    }

    private fun drawText(buffers: Renderer.Buffers, matrixStack: MatrixStack, r: Float, g: Float, b: Float, a: Float): Int {
        var i = 0
        val rgba = floatArrayOf(r, g, b, a)

        val lines = runText(rgba) { c, cx, cy ->
            val cw = fontRenderer.drawChar(buffers.triangleTexColor, c, cx, cy, rgba[0], rgba[1], rgba[2], rgba[3])

            // TODO: CURSOR NOT DRAWN ON INVISIBLE CHARS
            if(focused) {
                if(i++ == cursor) drawCursor(buffers, cx, cy)
                if(i == text.length && i == cursor) drawCursor(buffers, cx + cw, cy)
            }

            false
        }

        fontRenderer.bindTexture()
        buffers.triangleTexColor.draw(matrixStack)
        buffers.triangleTexColor.reset()

        buffers.lines.draw(matrixStack)
        buffers.lines.reset()

        return lines
    }

    private fun drawCursor(buffers: Renderer.Buffers, cx: Float, cy: Float) {
        if(System.currentTimeMillis() % 1060 >= 530) return
        buffers.lines.vertices(
            cx, cy, 0f, 1f, 1f, 1f, 1f, 1f,
            cx, cy + fontRenderer.charHeight, 0f, 1f, 1f, 1f, 1f, 1f
        )
        buffers.lines.indices(0, 1)
    }

    private fun runText(rgba: FloatArray, callback: (Char, Float, Float) -> Boolean): Int =
        fontRenderer.runSplitString(text, horzPadding, vertPadding, getWidth() - (horzPadding), vertPadding, rgba, callback)

    fun getText(): String = text
    fun setText(value: String) {
        text = value
    }

    fun getCursor(): Int = cursor
    fun setCursor(value: Int) {
        cursor = value
    }

    fun isFocused(): Boolean = focused
    fun setFocused(value: Boolean) {
        focused = value
    }

    // TODO: \n support for return/enter/line feed/line break/idkwhytherearesomanynamesforit
    // returns number of lines used
    private fun FontRenderer.runSplitString(text: String, x: Float, y: Float, wrapWidth: Float, padding: Float, rgba: FloatArray,
                                                                            callback: (Char, Float, Float) -> Boolean): Int {
        var lines = 1
        var currX = x
        var currY = y
        var first = true
        val split = text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for(part in split) {
            if(part.isEmpty()) continue

            if(!first) {
                if(callback(' ', currX, currY)) return lines
                currX += getCharWidth(' ')
            } else first = false

            val partWidth: Float = getStringWidth(part)
            if(partWidth > wrapWidth) {
                var i = 0
                while(i < part.length) {
                    val c = part[i]
                    if(c.code == 167 && i + 1 < part.length) color(part[++i], rgba)
                    else {
                        val cWidth: Float = getCharWidth(c)
                        if(cWidth + currX > wrapWidth) {
                            currX = x
                            currY += padding + charHeight
                            lines++
                        }

                        if(callback(c, currX, currY)) return lines
                        currX += cWidth
                    }
                    i++
                }
            } else {
                if(currX + partWidth > wrapWidth) {
                    currX = x
                    currY += padding + charHeight
                    lines++
                }

                var i = 0
                while(i < part.length) {
                    val c = part[i]
                    if(c.code == 167 && i + 1 < part.length) color(part[++i], rgba)
                    else {
                        if(callback(c, currX, currY)) return lines
                        currX += getCharWidth(c)
                    }
                    i++
                }
            }
        }

        return lines
    }
}
