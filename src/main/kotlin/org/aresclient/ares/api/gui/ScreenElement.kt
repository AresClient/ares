package org.aresclient.ares.api.gui

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean

abstract class AresScreen(title: Text?): Screen(title)
open class ScreenElement(title: String): Element() {
    private var open = false
    private var tooltip: Array<out String>? = null
    private var prevMouseX = 0
    private var prevMouseY = 0
    private var mouseTime = 0f

    private val matrixStack = MatrixStack()

    private val customScreen = object: AresScreen(Text.literal(title)) {
        init {
            RenderSystem.assertOnRenderThread()
        }

        override fun init() {
            open = true
            matrixStack.projection().setOrtho(0f, width.toFloat(), height.toFloat(), 0f, 1000f, 21000f)
            matrixStack.model().translation(0f, 0f, -11000f)
            this@ScreenElement.update()
            super.init()
        }

        override fun close() {
            this@ScreenElement.close()
            open = false
            super.close()
        }

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            tooltip = null

            if(mouseX == prevMouseX && mouseY == prevMouseY) mouseTime += delta
            else {
                prevMouseX = mouseX
                prevMouseY = mouseY
                mouseTime = 0f
            }

            // render children
            val theme = Theme.current()
            val state = Renderer.begin2d()
            Renderer.getStencilFramebuffer().resize(MC.framebuffer.textureWidth, MC.framebuffer.textureHeight)
            Renderer.getStencilFramebuffer().use {
                this@ScreenElement.draw(theme, state.buffers, matrixStack, mouseX, mouseY, delta) // we don't need to push matrices for screen drawing

                // draw tooltip
                if(tooltip?.isNotEmpty() == true && mouseTime > 10f) {
                    matrixStack.push()
                    matrixStack.model().translate(mouseX.toFloat(), mouseY.toFloat(), 0f)

                    val padding = 2f
                    val fontRenderer = theme.font.value.getRenderer()
                    val width = tooltip!!.maxOf { fontRenderer.getStringWidth(it, 10f) } + padding * 2
                    val height = tooltip!!.size * fontRenderer.getCharHeight(10f) + padding * 2

                    state.buffers.triangle.draw(matrixStack) {
                        vertices(
                            0f, -height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                            width, 0f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                            width, -height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                            0f, 0f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha
                        )
                        indices(
                            0, 1, 2,
                            0, 1, 3
                        )
                    }

                    state.buffers.lines.draw(matrixStack) {
                        vertices(
                            0f, -height, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                            width, -height, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                            width, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                            0f, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                        )
                        indices(0, 1, 1, 2, 2, 3, 3, 0)
                    }

                    fontRenderer.bindTexture()
                    state.buffers.triangleTexColor.draw(matrixStack) {
                        for((i, line) in tooltip!!.withIndex()) {
                            fontRenderer.drawString(this, line, 10f,
                                padding, padding - height + i * fontRenderer.getCharHeight(10f), theme.lightground.value)
                        }
                    }

                    matrixStack.pop()
                    tooltip = null
                }
            }
            Renderer.end(state) // cleanup
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
            this@ScreenElement.click(mouseX, mouseY, mouseButton, AtomicBoolean(false))
            return super.mouseClicked(mouseX, mouseY, mouseButton)
        }

        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
            this@ScreenElement.release(mouseX, mouseY, button)
            return super.mouseReleased(mouseX, mouseY, button)
        }

        override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            this@ScreenElement.type(null, keyCode)
            return super.keyPressed(keyCode, scanCode, modifiers)
        }

        // TODO: FIGURE OUT type callback
        override fun charTyped(chr: Char, modifiers: Int): Boolean {
            this@ScreenElement.type(chr, modifiers)
            return super.charTyped(chr, modifiers)
        }

        // horizontal amount???
        override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
            this@ScreenElement.scroll(mouseX, mouseY, verticalAmount, AtomicBoolean(false))
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }

        override fun shouldPause(): Boolean = false
    }

    override fun isVisible(): Boolean = open

    override fun getX(): Float = 0f
    override fun getY(): Float = 0f

    override fun getWidth(): Float = customScreen.width.toFloat()
    override fun getHeight(): Float = customScreen.height.toFloat()

    fun getScreen(): Screen = customScreen

    fun setTooltip(vararg tooltip: String) {
        this.tooltip = tooltip
    }
}
