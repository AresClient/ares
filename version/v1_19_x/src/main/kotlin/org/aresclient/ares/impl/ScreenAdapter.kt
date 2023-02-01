package org.aresclient.ares.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import org.aresclient.ares.api.ScreenContext
import org.aresclient.ares.renderer.Screen

class ScreenAdapter(private val screen: Screen): net.minecraft.client.gui.screen.Screen(MutableText.of(LiteralTextContent(screen.title))) {
    init {
        screen.setContext(this as ScreenContext)
    }

    override fun init() {
        screen.init()
    }

    override fun render(matrixStack: MatrixStack?, mouseX: Int, mouseY: Int, partialTicks: Float) {
        screen.render(mouseX, mouseY, partialTicks)
        super.render(matrixStack, mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        screen.click(mouseX.toInt(), mouseY.toInt(), mouseButton)
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        screen.release(mouseX.toInt(), mouseY.toInt(), mouseButton)
        return super.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        screen.type(null, keyCode)
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(character: Char, keycode: Int): Boolean {
        screen.type(character, keycode)
        return super.charTyped(character, keycode)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, value: Double): Boolean {
        screen.scroll(mouseX.toInt(), mouseY.toInt(), value * 15)
        return super.mouseScrolled(mouseX, mouseY, value)
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        screen.resize(width, height)
        super.resize(client, width, height)
    }

    override fun shouldPause(): Boolean {
        return screen.shouldPause()
    }

    override fun shouldCloseOnEsc(): Boolean {
        return screen.shouldCloseOnEsc()
    }

    override fun close() {
        screen.close()
        super.close()
    }
}