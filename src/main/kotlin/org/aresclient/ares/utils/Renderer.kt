package org.aresclient.ares.utils

import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.FontRenderer
import org.aresclient.ares.renderer.MatrixStack
import org.lwjgl.opengl.GL11
import java.awt.Font

object Renderer {
    // TODO: BETTER CHECK FOR LEGACY OPENGL
    private val LEGACY = Ares.MESH.loaderVersion.startsWith("1.12")
    private val FONT_RENDERERS = hashMapOf<Int, HashMap<Float, FontRenderer>>()
    private val FONT = Font.createFont(Font.TRUETYPE_FONT, Renderer::class.java.getResourceAsStream("/assets/ares/font/arial.ttf")) // TODO: CUSTOMIZE THIS

    fun getFontRenderer(size: Float, style: Int) = FONT_RENDERERS.getOrPut(style) { hashMapOf() }.getOrPut(size) { FontRenderer(FONT, size, style) }
    fun getFontRenderer(size: Float) = getFontRenderer(size, Font.PLAIN)

    inline fun render2d(callback: () -> Unit) {
        val state = begin(true)
        callback()
        state.end()
    }

    inline fun render3d(callback: (MatrixStack) -> Unit, disableCull: Boolean = true) {
        val state = begin(disableCull)

        // TODO: THIS DOES NOT WORK ON 1.12.2!!!
        val matrixStack = MatrixStack()
        Ares.MESH.renderer.camera.also {
            matrixStack.projection()
                .set(Ares.MESH.renderer.renderStack.projectionMatrix)
                .rotate(wrapDegrees(it.pitch).toRadians(), 1f, 0f, 0f)
                .rotate(wrapDegrees(it.yaw + 180f).toRadians(), 0f, 1f, 0f)
                .translate(-it.x.toFloat(), -it.y.toFloat(), -it.z.toFloat())
        }
        callback(matrixStack)

        state.end()
    }

    fun wrapDegrees(degrees: Float): Float{
        var wrapped = degrees % 360f
        if(wrapped >= 180f) wrapped -= 360f
        if(wrapped < -180f) wrapped += 360f
        return wrapped
    }

    fun Float.toRadians(): Float {
        return this / 180f * 3.1415927f
    }

    data class State(val depth: Boolean, val blend: Boolean, val cull: Boolean,  val texture: Boolean, val alpha/*LEGACY*/: Boolean)

    fun begin(disableCull: Boolean): State {
        if(LEGACY) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()

            GL11.glEnable(GL11.GL_ALPHA_TEST)
        }

        if(disableCull) GL11.glDisable(GL11.GL_CULL_FACE)

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

    inline fun clip(area: () -> Unit, callback: () -> Unit) {
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glStencilMask(0xFF)
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE)

        area()

        GL11.glStencilMask(0x00)
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF)

        callback()

        GL11.glDisable(GL11.GL_STENCIL_TEST)
    }
}
