package org.aresclient.ares.impl.gui.title

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.realms.gui.screen.RealmsMainScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.ngui.NScreenElement
import org.aresclient.ares.api.ngui.NStaticElement
import org.aresclient.ares.api.nrender.HudDrawer
import org.aresclient.ares.api.nrender.Textures
import org.aresclient.ares.impl.instrument.module.modules.misc.TitleScreen
import org.aresclient.ares.impl.util.Theme

class AresTitleScreen: NScreenElement("Ares Title Screen") {
    companion object: Wrapper {
        private val PANE = NStaticElement()

        private val BUTTONS = listOf(
            TitleButton(Text.translatable("menu.singleplayer"), 143f, 0f) {
                MC.setScreen(SelectWorldScreen(MC.currentScreen))
            },
            TitleButton(Text.translatable("menu.multiplayer"), 143f, 26f) {
                MC.setScreen(MultiplayerScreen(MC.currentScreen))
            },
            TitleButton(Text.translatable("menu.online"), 143f, 52f) {
                MC.setScreen(RealmsMainScreen(MC.currentScreen))
            },
            TitleButton(Text.of("Accounts"), 143f, 78f) {
                // TODO: make translation
                // TODO: CREATE ACCOUNTS GUI?
            },
            TitleButton(Text.translatable("menu.options"), 143f, 104f) {
                MC.setScreen(OptionsScreen(MC.currentScreen, MC.options))
            }
        )

        private val MINECRAFT_BUTTON = IconButton(Textures.minecraft, 5f, 5f, 30f, 30f) {
            TitleScreen.setEnabled(false)
            MC.setScreen(net.minecraft.client.gui.screen.TitleScreen())
        }

        private val EXIT_BUTTON = IconButton(Textures.exit, 0f, 5f, 30f, 30f) {
            MC.scheduleStop()
        }
    }

    init {
        BUTTONS.forEach { PANE.pushChild(it) }
        pushChild(PANE)

        pushChild(MINECRAFT_BUTTON)
        pushChild(EXIT_BUTTON)
    }

    override fun resize(width: Int, height: Int) {
        PANE.setX(getWidth() / 2f - 153f)
        PANE.setY(getHeight() / 2f - 63f)

        EXIT_BUTTON.setX(getWidth() - 35f)
    }

    override fun draw(theme: Theme, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val x = PANE.getX()
        val y = PANE.getY()
        HudDrawer.drawTexture(Textures.logo_bg, matrixStack, x, y, 126f, 126f, theme.secondary.value)
        HudDrawer.drawTexture(Textures.logo_fg, matrixStack, x, y, 126f, 126f, theme.primary.value)

        super.draw(theme, matrixStack, mouseX, mouseY, delta)
    }
}
