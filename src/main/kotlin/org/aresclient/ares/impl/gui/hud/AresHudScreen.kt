package org.aresclient.ares.impl.gui.hud

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.util.Color

class AresHudScreen: Screen(Text.of("Ares HUD Editor")), Wrapper {
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context.drawText(MC.textRenderer, "<insert hud editor here>", 0, 0, Color.WHITE.rgba, false)
    }

    override fun shouldPause(): Boolean = false
}
