package org.aresclient.ares.gui

import net.meshmc.mesh.util.render.Resolution
import net.meshmc.mesh.util.render.Screen
import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.*
import org.lwjgl.opengl.GL11

class AresTitleScreen: Screen("Ares Main Menu") {
    companion object {
        private val LEGACY_GL = Ares.MESH.loaderVersion.startsWith("1.12")
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

    private var resolution = Ares.MESH.minecraft.resolution

    init {
        resize(resolution)
    }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val fbo = Ares.MESH.minecraft.framebuffer.fbo

        if(LEGACY_GL) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()

            GL11.glEnable(GL11.GL_ALPHA_TEST)
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        MSAA.bind(fbo)

        SKYBOX.render(SKYBOX_STACK)
        SKYBOX_STACK.model().rotate((0.0002 * partialTicks).toFloat(), 0f, 1f, 0f)

        LOGO.bind()
        MATRIX_STACK.push()
        MATRIX_STACK.model().translate(resolution.scaledWidth / 2f, resolution.scaledHeight / 2f, 0f)
        MATRIX_STACK.model().scale(resolution.scaledWidth / 5f)
        BUFFER.draw(MATRIX_STACK)
        MATRIX_STACK.pop()

        MSAA.blit(fbo)

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)

        if(LEGACY_GL) {
            GL11.glDisable(GL11.GL_ALPHA_TEST)

            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    override fun resize(width: Int, height: Int) = resize(Ares.MESH.minecraft.resolution)

    private fun resize(resolution: Resolution) {
        this.resolution = resolution

        SKYBOX_STACK = MatrixStack().also {
            it.projection().perspective(
                1.5f,
                (resolution.scaledWidth / resolution.scaledHeight).toFloat(),
                0.1f,
                2f
            )
            it.model().rotate(Math.toRadians(45.0).toFloat(), 0f, 1f, 0f)
        }

        MATRIX_STACK = MatrixStack().also {
            it.projection().ortho(0F, resolution.scaledWidth.toFloat(), resolution.scaledHeight.toFloat(), 0F, 0F, 1F)
        }

        MSAA.resize(resolution)
    }
}
