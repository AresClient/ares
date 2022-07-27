package org.aresclient.ares.utils

import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.*
import org.lwjgl.opengl.GL11
import java.awt.Font

object Renderer {
    // TODO: BETTER CHECK FOR LEGACY OPENGL
    private val LEGACY = Ares.MESH.loaderVersion.startsWith("1.12")
    private val FONT_RENDERERS = hashMapOf<Int, HashMap<Float, FontRenderer>>()
    private val FONT = Font.createFont(Font.TRUETYPE_FONT, Renderer::class.java.getResourceAsStream("/assets/ares/font/arial.ttf")) // TODO: CUSTOMIZE THIS

    fun getFontRenderer(size: Float, style: Int) = FONT_RENDERERS.getOrPut(style) { hashMapOf() }.getOrPut(size) { FontRenderer(FONT, size, style) }
    fun getFontRenderer(size: Float) = getFontRenderer(size, Font.PLAIN)

    data class Uniforms(val roundedRadius: Uniform.F1, val roundedSize: Uniform.F2)
    data class Buffers(val triangle: Buffer, val triangleTex: Buffer, val triangleTexColor: Buffer, val ellipse: Buffer, val rounded: Buffer, val lines: Buffer, val uniforms: Uniforms)
    val BUFFERS by lazy {
        val roundedRadius = Shader.ROUNDED.uniformF1("radius")
        val roundedSize = Shader.ROUNDED.uniformF2("size")
        Buffers(
            Buffer.createDynamic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR),
            Buffer.createDynamic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV),
            Buffer.createDynamic(Shader.POSITION_TEXTURE_COLOR, VertexFormat.POSITION_UV_COLOR),
            Buffer.createDynamic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR),
            Buffer.createDynamic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR).uniform(roundedRadius).uniform(roundedSize),
            Buffer.createDynamic(Shader.LINES, VertexFormat.LINES).lines(),
            Uniforms(
                roundedRadius,
                roundedSize
            )
        )
    }

    inline fun Buffer.draw(matrixStack: MatrixStack? = null, callback: Buffer.() -> Unit) {
        callback()
        draw(matrixStack ?: MatrixStack.EMPTY)
        reset()
    }

    fun Buffers.reset() {
        triangle.reset()
        triangleTex.reset()
        triangleTexColor.reset()
        ellipse.reset()
        lines.reset()
    }

    fun Buffers.draw(matrixStack: MatrixStack?) {
        if(triangle.shouldRender()) triangle.draw(matrixStack ?: MatrixStack.EMPTY)
        if(triangleTex.shouldRender()) triangleTex.draw(matrixStack ?: MatrixStack.EMPTY)
        if(triangleTexColor.shouldRender()) triangleTexColor.draw(matrixStack ?: MatrixStack.EMPTY)
        if(ellipse.shouldRender()) ellipse.draw(matrixStack ?: MatrixStack.EMPTY)
        if(rounded.shouldRender()) rounded.draw(matrixStack ?: MatrixStack.EMPTY)
        if(lines.shouldRender()) lines.draw(matrixStack ?: MatrixStack.EMPTY)
    }

    inline fun render(matrixStack: MatrixStack? = null, callback: (Buffers) -> Unit) {
        val state = begin()

        callback(BUFFERS)
        BUFFERS.draw(matrixStack)
        BUFFERS.reset()

        state.end()
    }

    inline fun render3d(callback: (Buffers, MatrixStack) -> Unit) {
        val state = begin()

        // TODO: THIS DOES NOT WORK ON 1.12.2!!!
        val matrixStack = MatrixStack()
        Ares.MESH.renderer.camera.also {
            matrixStack.projection()
                .set(Ares.MESH.renderer.renderStack.projectionMatrix)
                .rotate(wrapDegrees(it.pitch).toRadians(), 1f, 0f, 0f)
                .rotate(wrapDegrees(it.yaw + 180f).toRadians(), 0f, 1f, 0f)
                .translate(-it.x.toFloat(), -it.y.toFloat(), -it.z.toFloat())
        }

        callback(BUFFERS, matrixStack)
        BUFFERS.draw(matrixStack)
        BUFFERS.reset()

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

    data class State(val depth: Boolean, val blend: Boolean, val cull: Boolean, val alpha: Boolean)

    fun begin(): State {
        val state = State(
            GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
            GL11.glIsEnabled(GL11.GL_BLEND),
            GL11.glIsEnabled(GL11.GL_CULL_FACE),
            if(LEGACY) GL11.glIsEnabled(GL11.GL_ALPHA_TEST) else false
        )

        if(LEGACY) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()

            GL11.glDisable(GL11.GL_ALPHA_TEST)
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColorMask(true, true, true, true)

        return state
    }

    fun State.end() {
        depth.set(GL11.GL_DEPTH_TEST)
        blend.set(GL11.GL_BLEND)
        cull.set(GL11.GL_CULL_FACE)

        if(LEGACY) {
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
