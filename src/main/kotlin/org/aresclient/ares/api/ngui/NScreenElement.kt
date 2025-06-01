package org.aresclient.ares.api.ngui

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.aresclient.ares.api.gui.AresScreen
import org.aresclient.ares.api.nrender.hud.HudDrawer
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean

open class NScreenElement(title: String): NElement() {
    private var open = false

    private val customScreen = object: AresScreen(Text.literal(title)) {
        init {
            RenderSystem.assertOnRenderThread()
        }

        override fun init() {
            open = true
            this@NScreenElement.resize(width, height)
            super.init()
        }

        override fun close() {
            this@NScreenElement.close()
            open = false
            super.close()
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            renderBackground(context, mouseX, mouseY, delta)
            this@NScreenElement.render(Theme.current(), context.matrices, mouseX, mouseY, delta)
            HudDrawer.draw()
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
            this@NScreenElement.click(mouseX, mouseY, mouseButton, AtomicBoolean(false))
            return super.mouseClicked(mouseX, mouseY, mouseButton)
        }

        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
            this@NScreenElement.release(mouseX, mouseY, button)
            return super.mouseReleased(mouseX, mouseY, button)
        }

        override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            this@NScreenElement.type(null, keyCode)
            return super.keyPressed(keyCode, scanCode, modifiers)
        }

        override fun charTyped(chr: Char, modifiers: Int): Boolean {
            this@NScreenElement.type(chr, modifiers)
            return super.charTyped(chr, modifiers)
        }

        // horizontal amount???
        override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
            this@NScreenElement.scroll(mouseX, mouseY, verticalAmount, AtomicBoolean(false))
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }

        override fun resize(client: MinecraftClient?, width: Int, height: Int) {
            this@NScreenElement.resize(width, height)
            super.resize(client, width, height)
        }

        override fun shouldPause(): Boolean = false
    }

    override fun isVisible(): Boolean = open

    override fun getX(): Float = 0f

    override fun getY(): Float = 0f

    override fun getWidth(): Float = customScreen.width.toFloat()

    override fun getHeight(): Float = customScreen.height.toFloat()

    fun getScreen(): Screen = customScreen
}
