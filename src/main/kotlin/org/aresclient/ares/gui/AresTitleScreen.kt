package org.aresclient.ares.gui

import net.meshmc.mesh.api.render.Screen
import org.aresclient.ares.Ares
import org.aresclient.ares.gui.api.*
import org.aresclient.ares.renderer.*

class AresTitleScreen: ScreenElement("Ares Main Menu") {
    companion object {
        private val SKYBOX = SkyBox("/assets/ares/textures/panorama/2b2t")
        private val SKYBOX_STACK = MatrixStack().also { it.model().rotate(Math.toRadians(45.0).toFloat(), 0f, 1f, 0f) }

        private val LOGO = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares.png"))
        private val IMAGE = Image(LOGO, 0f, 0f, 126f, 126f)

        private val PANE = StaticElement()

        private val BUTTONS = listOf(
            TitleScreenButton("Singleplayer", 143f, 0f) {
                Screen.openSelectWorldScreen()
            },
            TitleScreenButton("Multiplayer", 143f, 26f) {
                Screen.openMultiplayerScreen()
            },
            TitleScreenButton("Realms", 143f, 52f) {
                Screen.openRealmsMainScreen()
            },
            TitleScreenButton("Accounts", 143f, 78f) {
                // TODO: CREATE ACCOUNTS GUI?
            },
            TitleScreenButton("Options", 143f, 104f) {
                Screen.openOptionsScreen()
            }
        )

        private val MINECRAFT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/minecraft.png")),
            5f, 5f, 30f, 30f,
            Ares.GRAY, Ares.RED
        ) {
            Ares.CUSTOM_MAIN_MENU.value = false
            Screen.openTitleScreen()
        }

        private val EXIT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/exit.png")),
            0f, 5f, 30f, 30f,
            Ares.GRAY, Ares.RED
        ) {
            Ares.MESH.minecraft.shutdown()
        }
    }

    override fun init() {
        PANE.pushChild(IMAGE)
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

    override fun draw(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        SKYBOX.render(SKYBOX_STACK)
        SKYBOX_STACK.model().rotate((0.0002 * delta).toFloat(), 0f, 1f, 0f)

        super.draw(matrixStack, mouseX, mouseY, delta)
    }
}
