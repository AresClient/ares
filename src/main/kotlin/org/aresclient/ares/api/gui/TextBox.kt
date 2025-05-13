package org.aresclient.ares.api.gui

import dev.tigr.simpleevents.listener.EventListener
import dev.tigr.simpleevents.listener.Priority
import org.aresclient.ares.Ares
import org.aresclient.ares.api.events.InputEvent
import org.aresclient.ares.api.render.FontRenderer
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Keys
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.atomic.AtomicBoolean

// this is so scuffed, should probably be rewritten
open class TextBox(x: Float, y: Float, width: Float, private val fontSize: Float, private val minLines: Int,
                   private val vertPadFactor: Float = 0.2f, private val horizPadFactor: Float = 0.5f): StaticElement(x, y, width, 0f) {

    private val vertPadding: Float
        get() = getFontRenderer().getCharHeight(fontSize) * vertPadFactor
    private val horzPadding: Float
        get() = getFontRenderer().getCharHeight(fontSize) * horizPadFactor

    private var text = ""
    private var lines = minLines
    private var cursor = 0
    private var focused = false

    // prevent binds from being invoked when textbox is focused
    private val inputEventListener = EventListener<InputEvent>(Priority.HIGH) {
        if(focused) it.textboxFocused = true
    }

    init {
        Ares.EVENT_MANAGER.register(inputEventListener)
    }

    private fun getFontRenderer() = Theme.current().font.value.getRenderer()

    override fun getHeight(): Float = (getFontRenderer().getCharHeight(fontSize) + vertPadding) * lines + vertPadding

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val width = getWidth()
        val height = getHeight()

        buffers.triangle.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                width, 0f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                0f, height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                width, height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        buffers.lines.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width, height, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                0f, height, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(
                0, 1,
                1, 2,
                2, 3,
                3, 0
            )
        }

        lines = max(drawText(buffers, matrixStack, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha), minLines)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        super.click(mouseX, mouseY, mouseButton, acted)

        if(!isMouseOver(mouseX, mouseY)) {
            focused = false
            return
        }

        if(!acted.get() && mouseButton == 0) {
            var i = 0
            val unused = FloatArray(4)
            val rx = getRenderX()
            val ry = getRenderY()

            val fontRenderer = getFontRenderer()
            runText(unused) { c, cx, cy ->
                val cw = fontRenderer.getCharWidth(c, fontSize)
                if(mouseY >= cy + ry && mouseY <= cy + ry + fontRenderer.getCharHeight(fontSize) && mouseX >= cx + rx && mouseX <= cx + rx + cw) {
                    cursor = if(mouseX < cx + rx + cw / 2f) i else i + 1
                    return@runText true
                }

                i++
                false
            }

            focused = true
            acted.set(true)
        }
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

        if(text.isNotEmpty()) {
            val fontRenderer = getFontRenderer()
            val lines = runText(rgba) { c, cx, cy ->
                val cw = fontRenderer.drawChar(buffers.triangleTexColor, c, fontSize, cx, cy, rgba[0], rgba[1], rgba[2], rgba[3])

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
        } else {
            drawCursor(buffers, horzPadding, vertPadding)

            buffers.lines.draw(matrixStack)
            buffers.lines.reset()

            return 1
        }
    }

    private fun drawCursor(buffers: Renderer.Buffers, cx: Float, cy: Float) {
        if(System.currentTimeMillis() % 1060 >= 530) return
        buffers.lines.vertices(
            cx, cy, 0f, 1f, 1f, 1f, 1f, 1f,
            cx, cy + getFontRenderer().getCharHeight(fontSize), 0f, 1f, 1f, 1f, 1f, 1f
        )
        buffers.lines.indices(0, 1)
    }

    private fun runText(rgba: FloatArray, callback: (Char, Float, Float) -> Boolean): Int =
        getFontRenderer().runSplitString(text, horzPadding, vertPadding, getWidth() - (horzPadding), vertPadding, rgba, callback)

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
                currX += getCharWidth(' ', fontSize)
            } else first = false

            val partWidth: Float = getStringWidth(part, fontSize)
            if(partWidth > wrapWidth) {
                var i = 0
                while(i < part.length) {
                    val c = part[i]
                    if(c.code == 167 && i + 1 < part.length) color(part[++i], rgba)
                    else {
                        val cWidth: Float = getCharWidth(c, fontSize)
                        if(cWidth + currX > wrapWidth) {
                            currX = x
                            currY += padding + getCharHeight(fontSize)
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
                    currY += padding + getCharHeight(fontSize)
                    lines++
                }

                var i = 0
                while(i < part.length) {
                    val c = part[i]
                    if(c.code == 167 && i + 1 < part.length) color(part[++i], rgba)
                    else {
                        if(callback(c, currX, currY)) return lines
                        currX += getCharWidth(c, fontSize)
                    }
                    i++
                }
            }
        }

        return lines
    }
}
