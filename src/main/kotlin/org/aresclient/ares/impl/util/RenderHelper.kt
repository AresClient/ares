package org.aresclient.ares.impl.util

import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.DemoScreen
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.realms.gui.screen.RealmsMainScreen
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.render.Buffer
import org.aresclient.ares.api.render.FontRenderer
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import java.awt.Font

object RenderHelper: Wrapper {
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

    fun openChatScreen(input: String?) {
        MC.setScreen(ChatScreen(input))
    }

    fun openDemoScreen() {
        MC.setScreen(DemoScreen())
    }

    fun openMultiplayerScreen() {
        MC.setScreen(MultiplayerScreen(MC.currentScreen))
    }

    fun openOptionsScreen() {
        MC.setScreen(OptionsScreen(MC.currentScreen, MC.options))
    }

    fun openSelectWorldScreen() {
        MC.setScreen(SelectWorldScreen(MC.currentScreen))
    }

    fun openRealmsMainScreen() {
        MC.setScreen(RealmsMainScreen(MC.currentScreen))
    }

    fun openTitleScreen() {
        MC.setScreen(TitleScreen())
    }
}