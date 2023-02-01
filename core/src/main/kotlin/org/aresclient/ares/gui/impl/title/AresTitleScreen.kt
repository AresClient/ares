package org.aresclient.ares.gui.impl.title

import org.aresclient.ares.Ares
import org.aresclient.ares.gui.api.ScreenElement
import org.aresclient.ares.gui.api.StaticElement
import org.aresclient.ares.renderer.*
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class AresTitleScreen: ScreenElement("Ares Title Screen") {
    companion object {
        private val SKYBOX = SkyBox("/assets/ares/textures/panorama/2b2t")
        private val SKYBOX_STACK = MatrixStack().also { it.model().rotate(Math.toRadians(45.0).toFloat(), 0f, 1f, 0f) }

        private val LOGO_BG = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares_bg.png"))
        private val LOGO_FG = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares_fg.png"))

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
            Ares.TITLE_SETTING.value = false
            Screen.openTitleScreen()
        }

        private val EXIT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/exit.png")),
            0f, 5f, 30f, 30f
        ) {
            Ares.INSTANCE.minecraft.shutdown()
        }
    }

    override fun init() {
        BUTTONS.forEach { PANE.pushChild(it) }
        pushChild(PANE)

        pushChild(MINECRAFT_BUTTON)
        pushChild(EXIT_BUTTON)
    }

    override fun update() {
        SKYBOX_STACK.projection().setPerspective(
            1.5f,
            getWidth() / getHeight(),
            0.1f,
            2f
        )

        PANE.setX(getWidth() / 2f - 153f)
        PANE.setY(getHeight() / 2f - 63f)

        EXIT_BUTTON.setX(getWidth() - 35f)

        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        SKYBOX.render(SKYBOX_STACK)
        SKYBOX_STACK.model().rotate((0.0002 * delta).toFloat(), 0f, 1f, 0f)

        matrixStack.push()
        matrixStack.model().translate(PANE.getX(), PANE.getY(), 0f)
        buffers.triangleTexColor.drawHelmet(matrixStack, LOGO_BG, theme.secondary.getColor())
        buffers.triangleTexColor.drawHelmet(matrixStack, LOGO_FG, theme.primary.getColor())
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
