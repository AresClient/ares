package org.aresclient.ares.impl.instrument.module.modules.render

import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.Renderer.Buffers
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.api.util.Keys
import org.aresclient.ares.impl.gui.impl.title.TitleButton
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.lwjgl.opengl.GL11

// a simple mesh test module
object TestModule: Module(Category.RENDER, "Test", "A simple test module", Defaults().setBind(Keys.Y).setEnabled(true)) {
    private val renderer by lazy { RenderHelper.getFontRenderer(24f) }
    private val text = settings.addString("Text", "Hello World!")
    private val color = settings.addColor("Color", Color.WHITE)

    override fun onRenderHud(delta: Float, buffers: Renderer.Buffers, matrixStack: MatrixStack) {
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE)
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF)
        GL11.glStencilMask(0xFF)

        matrixStack.push()
        matrixStack.model().translate(100f, 100f, 0f).scale(100f)
        buffers.uniforms.roundedCutoff.set(1f)
        draw(buffers, matrixStack, color.value)

        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF)
        GL11.glStencilMask(0x00)

        matrixStack.model()
            .scale(1.1f, 1.1f, 1.1f)
        buffers.uniforms.roundedCutoff.set(0f)
        draw(buffers, matrixStack, Color.WHITE)
        matrixStack.pop()

        GL11.glDisable(GL11.GL_STENCIL_TEST)

        renderer.drawString(matrixStack, text.value, 100f, 0f, color.value)
    }

    fun draw(buffers: Buffers, matrixStack: MatrixStack, color: Color) {
        buffers.uniforms.roundedRadius.set(0.2f)
        buffers.uniforms.roundedSize.set(1f, 1f)
        buffers.rounded.draw(matrixStack) {
            vertices(
                -0.5f, -0.5f, 0f, -1f, -1f,   color.red, color.green, color.blue, color.alpha,
                0.5f, -0.5f, 0f, 1f, -1f,     color.red, color.green, color.blue, color.alpha,
                0.5f, 0.5f, 0f, 1f, 1f,       color.red, color.green, color.blue, color.alpha,
                -0.5f, 0.5f, 0f, -1f, 1f,     color.red, color.green, color.blue, color.alpha
            )
            indices(
                0, 1, 2,
                0, 2, 3
            )
        }
    }
}
