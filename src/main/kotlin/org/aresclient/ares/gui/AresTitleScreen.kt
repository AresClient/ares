package org.aresclient.ares.gui

import net.meshmc.mesh.api.render.Screen
import org.aresclient.ares.Ares
import org.aresclient.ares.gui.api.IconButton
import org.aresclient.ares.gui.api.TitleScreenButton
import org.aresclient.ares.renderer.*
import org.aresclient.ares.utils.Renderer

class AresTitleScreen: Screen("Ares Main Menu") {
    companion object {
        private val SKYBOX = SkyBox("/assets/ares/textures/panorama/2b2t")
        private lateinit var SKYBOX_STACK: MatrixStack
        private lateinit var MATRIX_STACK: MatrixStack

        private val LOGO = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/ares.png"))
        private val IMAGE = Buffer
            .beginStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                0f, 0f, 0f, 0f, 0f,
                0f, 126f, 0f, 0f, 1f,
                126f, 126f, 0f, 1f, 1f,
                126f, 0f, 0f, 1f, 0f
            )
            .indices(
                0, 1, 2,
                2, 0, 3
            )
            .end()

        private val BUTTONS = listOf(
            TitleScreenButton("Singleplayer", 143f, 0f) {
                openSelectWorldScreen()
            },
            TitleScreenButton("Multiplayer", 143f, 26f) {
                openMultiplayerScreen()
            },
            TitleScreenButton("Realms", 143f, 52f) {
                openRealmsMainScreen()
            },
            TitleScreenButton("Accounts", 143f, 78f) {
                // TODO: CREATE ACCOUNTS GUI?
            },
            TitleScreenButton("Options", 143f, 104f) {
                openOptionsScreen()
            }
        )

        private val MINECRAFT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/minecraft.png")),
            Ares.GRAY, Ares.RED,
            5f, 5f, 30f, 30f
        ) {
            Ares.CUSTOM_MAIN_MENU.value = !Ares.CUSTOM_MAIN_MENU.value
            openTitleScreen()
        }

        private val EXIT_BUTTON = IconButton(
            Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/exit.png")),
            Ares.GRAY, Ares.RED,
            0f, 5f, 30f, 30f
        ) {
            Ares.MESH.minecraft.shutdown()
        }
    }

    override fun init() {
        SKYBOX_STACK = MatrixStack().also {
            it.projection().perspective(
                1.5f,
                (width / height).toFloat(),
                0.1f,
                2f
            )
            it.model().rotate(Math.toRadians(45.0).toFloat(), 0f, 1f, 0f)
        }

        MATRIX_STACK = MatrixStack().also {
            it.projection().ortho(0F, width.toFloat(), height.toFloat(), 0F, 0F, 1F)
        }

        EXIT_BUTTON.x = width - 35f
    }

    private fun paneX() = width / 2f - 153f
    private fun paneY() = height / 2f - 63f

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) = Renderer.render2d {
        Ares.MESH.minecraft.framebuffer.clear()

        // draw cool background
        SKYBOX.render(SKYBOX_STACK)
        SKYBOX_STACK.model().rotate((0.0002 * partialTicks).toFloat(), 0f, 1f, 0f)

        // begin drawing main pane
        val paneX = paneX()
        val paneY = paneY()
        MATRIX_STACK.push()
        MATRIX_STACK.model().translate(paneX, paneY, 0f)

        // draw logo
        LOGO.bind()
        IMAGE.draw(MATRIX_STACK)

        // draw nav buttons
        BUTTONS.forEach { it.render(MATRIX_STACK, mouseX.toFloat(), mouseY.toFloat(), offsetX = paneX, offsetY = paneY) }

        MATRIX_STACK.pop()

        MINECRAFT_BUTTON.render(MATRIX_STACK, mouseX.toFloat(), mouseY.toFloat())
        EXIT_BUTTON.render(MATRIX_STACK, mouseX.toFloat(), mouseY.toFloat())
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        BUTTONS.forEach { it.click(mouseX - paneX(), mouseY - paneY(), mouseButton) }
        MINECRAFT_BUTTON.click(mouseX.toFloat(), mouseY.toFloat(), mouseButton)
        EXIT_BUTTON.click(mouseX.toFloat(), mouseY.toFloat(), mouseButton)
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        BUTTONS.forEach { it.release(mouseX - paneX(), mouseY - paneY(), mouseButton) }
        MINECRAFT_BUTTON.release(mouseX.toFloat(), mouseY.toFloat(), mouseButton)
        EXIT_BUTTON.release(mouseX.toFloat(), mouseY.toFloat(), mouseButton)
    }
}
