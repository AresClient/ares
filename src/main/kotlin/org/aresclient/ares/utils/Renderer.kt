package org.aresclient.ares.utils

import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.FontRenderer
import org.aresclient.ares.renderer.MatrixStack
import org.lwjgl.opengl.GL11
import java.awt.Font


object Renderer {
    // TODO: BETTER CHECK FOR LEGACY OPENGL
    private val LEGACY = Ares.MESH.loaderVersion.startsWith("1.12")
    private val FONT_RENDERERS = hashMapOf<Float, FontRenderer>()
    private val FONT = Font.createFont(Font.TRUETYPE_FONT, Renderer::class.java.getResourceAsStream("/assets/ares/font/arial.ttf")) // TODO: CUSTOMIZE THIS

    fun getFontRenderer(size: Float) = FONT_RENDERERS.getOrPut(size) { FontRenderer(FONT, size) }

    inline fun render2d(callback: () -> Unit) {
        val state = begin()
        callback()
        state.end()
    }

    inline fun render3d(callback: (MatrixStack) -> Unit) {
        val state = begin()

        // TODO: THIS IS INCORRECT I THINK
        val matrixStack = MatrixStack()
        Ares.MESH.renderer.camera.also {
            matrixStack.projection().translate(-it.x.toFloat(), -it.y.toFloat(), -it.z.toFloat())
        }
        callback(matrixStack)

        state.end()
    }

    data class State(val depth: Boolean, val blend: Boolean, val cull: Boolean,  val texture: Boolean, val alpha/*LEGACY*/: Boolean)

    fun begin(): State {
        if(LEGACY) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()

            GL11.glEnable(GL11.GL_ALPHA_TEST)
        }

        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        return State(
            GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
            GL11.glIsEnabled(GL11.GL_BLEND),
            GL11.glIsEnabled(GL11.GL_CULL_FACE),
            if(LEGACY) GL11.glIsEnabled(GL11.GL_TEXTURE_2D) else false,
            if(LEGACY) GL11.glIsEnabled(GL11.GL_ALPHA_TEST) else false
        )
    }

    fun State.end() {
        depth.set(GL11.GL_DEPTH_TEST)
        blend.set(GL11.GL_BLEND)
        cull.set(GL11.GL_CULL_FACE)

        if(LEGACY) {
            texture.set(GL11.GL_TEXTURE_2D)
            alpha.set(GL11.GL_ALPHA_TEST)

            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
        }
    }

    private fun Boolean.set(type: Int) {
        if(this) GL11.glEnable(type)
        else GL11.glDisable(type)
    }

    inline fun scissor(x: Float, y: Float, width: Float, height: Float, callback: () -> Unit) {
        val framebuffer = Ares.MESH.minecraft.framebuffer
        val resolution = Ares.MESH.minecraft.resolution
        val scaleWidth = framebuffer.width.toFloat() / resolution.scaledWidth.toFloat()
        val scaleHeight = framebuffer.height.toFloat() / resolution.scaledHeight.toFloat()

        GL11.glScissor(
            (x * scaleWidth).toInt(),
            framebuffer.height - ((y + height) * scaleHeight).toInt(),
            (width * scaleWidth).toInt(),
            (height * scaleHeight).toInt()
        )
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        callback()
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }
}
