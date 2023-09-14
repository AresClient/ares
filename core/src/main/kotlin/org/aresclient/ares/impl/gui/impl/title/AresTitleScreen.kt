package org.aresclient.ares.impl.gui.impl.title

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.minecraft.render.Screen
import org.aresclient.ares.api.render.*
import org.aresclient.ares.impl.gui.api.StaticElement
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.gui.api.ScreenElement
import org.aresclient.ares.impl.gui.impl.AresSkybox
import org.aresclient.ares.impl.instrument.module.modules.misc.TitleScreen

class AresTitleScreen: ScreenElement("Ares Title Screen") {
    companion object {
        private val LOGO_BG =
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares_bg.png"))
        private val LOGO_FG =
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares_fg.png"))

        private val PANE = StaticElement()

        private val BUTTONS = listOf(
            TitleButton("Singleplayer", 143f, 0f) {
                Screen.openSelectWorldScreen()
            },
            TitleButton("Multiplayer", 143f, 26f) {
                Screen.openMultiplayerScreen()
            },
            TitleButton("Realms", 143f, 52f) {
                Screen.openRealmsMainScreen()
            },
            TitleButton("Accounts", 143f, 78f) {
                // TODO: CREATE ACCOUNTS GUI?
            },
            TitleButton("Options", 143f, 104f) {
                Screen.openOptionsScreen()
            }
        )

        private val MINECRAFT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/minecraft.png")),
            5f, 5f, 30f, 30f
        ) {
            TitleScreen.isEnabled = false
            Screen.openTitleScreen()
        }

        private val EXIT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/exit.png")),
            0f, 5f, 30f, 30f
        ) {
            Ares.getMinecraft().shutdown()
        }
    }

    init {
        BUTTONS.forEach { PANE.pushChild(it) }
        pushChild(PANE)

        pushChild(MINECRAFT_BUTTON)
        pushChild(EXIT_BUTTON)
    }

    override fun update() {
        AresSkybox.update(getWidth(), getHeight())

        PANE.setX(getWidth() / 2f - 153f)
        PANE.setY(getHeight() / 2f - 63f)

        EXIT_BUTTON.setX(getWidth() - 35f)
    }
    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        AresSkybox.draw(delta)

        matrixStack.push()
        matrixStack.model().translate(PANE.getX(), PANE.getY(), 0f)
        buffers.triangleTexColor.drawHelmet(matrixStack, LOGO_BG, theme.secondary.value)
        buffers.triangleTexColor.drawHelmet(matrixStack, LOGO_FG, theme.primary.value)
        matrixStack.pop()

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    private fun Buffer.drawHelmet(matrixStack: MatrixStack, texture: Texture, color: Color) {
        texture.bind()
        draw(matrixStack) {
            vertices(
                0f, 0f, 0f, 0f, 0f, color.red, color.green, color.blue, color.alpha,
                0f, 126f, 0f, 0f, 1f, color.red, color.green, color.blue, color.alpha,
                126f, 126f, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
                126f, 0f, 0f, 1f, 0f, color.red, color.green, color.blue, color.alpha
            )
            indices(
                0, 1, 2,
                2, 0, 3
            )
        }
    }
}
