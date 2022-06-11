package org.aresclient.ares.gui

import net.meshmc.mesh.api.render.Screen
import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.*
import org.aresclient.ares.utils.render2d

class AresTitleScreen: Screen("Ares Main Menu") {
    companion object {
        private val SKYBOX = SkyBox("/assets/ares/textures/panorama/2b2t")
        private val MSAA = MSAAFrameBuffer(4, Ares.MESH.minecraft.resolution)
        private lateinit var SKYBOX_STACK: MatrixStack
        private lateinit var MATRIX_STACK: MatrixStack

        private val LOGO = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/menu_logo.png"))
        private val BUFFER = Buffer
            .beginStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV)
            .vertices(
                -1f, -1f, 0f, 0f, 0f,
                -1f, 1f, 0f, 0f, 1f,
                1f, 1f, 0f, 1f, 1f,
                1f, -1f, 0f, 1f, 0f
            )
            .indices(
                0, 1, 2,
                2, 0, 3
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

        MSAA.resize(Ares.MESH.minecraft.resolution)
    }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val fbo = Ares.MESH.minecraft.framebuffer.also { it.clear() }.fbo

        render2d {
            MSAA.bind(fbo) // TODO: IMPROVE LOGO RESOLUTION SO THIS IS ACTUALLY USED

            SKYBOX.render(SKYBOX_STACK)
            SKYBOX_STACK.model().rotate((0.0002 * partialTicks).toFloat(), 0f, 1f, 0f)

            LOGO.bind()
            MATRIX_STACK.push()
            MATRIX_STACK.model().translate(width / 2f, height / 2f, 0f)
            MATRIX_STACK.model().scale(width / 5f)
            BUFFER.draw(MATRIX_STACK)
            MATRIX_STACK.pop()

            MSAA.blit(fbo)
        }
    }
}
