package org.aresclient.ares.gui

import net.meshmc.mesh.api.render.Screen
import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.*
import org.aresclient.ares.utils.Renderer

class AresTitleScreen: Screen("Ares Main Menu") {
    companion object {
        private val SKYBOX = SkyBox("/assets/ares/textures/panorama/2b2t")
        private lateinit var SKYBOX_STACK: MatrixStack
        private lateinit var MATRIX_STACK: MatrixStack

        private val LOGO = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/ares_hex.png")) // 903 x 1042
        private val BUFFER = Buffer
            .beginStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                0f, 0f, 0f, 0f, 0f,
                0f, 100f, 0f, 0f, 1f,
                903f / 1042f * 100f, 100f, 0f, 1f, 1f,
                903f / 1042f * 100f, 0f, 0f, 1f, 0f
            )
            .indices(
                0, 1, 2,
                2, 0, 3
            )
            .end()

        // TODO: UPDATE ROUNDED SHADER TO ALLOW NON-SQUARE RECTANGLES TO NOT BE DISTORTED
        private val BUTTON = Buffer
            .beginStatic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR, 8, 12)
            .vertices(
                110f, 22f, 0f, /*1f, 1f,*/ 0.54f, 0.03f, 0.03f, 1f,
                110f, 0f, 0f, /*1f, -1f,*/ 0.54f, 0.03f, 0.03f, 1f,
                0f,  22f, 0f, /*-1f, 1f,*/ 0.54f, 0.03f, 0.03f, 1f,
                0f, 0f, 0f, /*-1f, -1f,*/ 0.54f, 0.03f, 0.03f, 1f,

                109f, 21f, 0f, /*1f, 1f,*/ 0f, 0f, 0f, 1f,
                109f, 1f, 0f, /*1f, -1f,*/ 0f, 0f, 0f, 1f,
                1f,  21f, 0f, /*-1f, 1f,*/ 0f, 0f, 0f, 1f,
                1f, 1f, 0f, /*-1f, -1f,*/ 0f, 0f, 0f, 1f
            )
            .indices(
                0, 1, 2,
                1, 2, 3,
                4, 5, 6,
                5, 6, 7,
            )
            .end()
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
    }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        Ares.MESH.minecraft.framebuffer.clear()

        Renderer.render2d {
            SKYBOX.render(SKYBOX_STACK)
            SKYBOX_STACK.model().rotate((0.0002 * partialTicks).toFloat(), 0f, 1f, 0f)

            MATRIX_STACK.push()

            LOGO.bind()
            MATRIX_STACK.model().translate(width / 2f - 110f, height / 2f - 50f, 0f)
            BUFFER.draw(MATRIX_STACK)

            // nav buttons:
            MATRIX_STACK.model().translate(115f, 0f, 0f)
            BUTTON.draw(MATRIX_STACK)

            MATRIX_STACK.pop()
        }
    }
}
