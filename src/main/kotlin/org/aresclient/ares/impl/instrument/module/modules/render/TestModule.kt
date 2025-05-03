package org.aresclient.ares.impl.instrument.module.modules.render

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.Renderer.Buffers
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.api.util.Keys
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw

object TestModule: Module(Category.RENDER, "Test", "A simple test module", Defaults().setBind(Keys.Y).setEnabled(true)) {
    private val font by lazy { RenderHelper.getFontRenderer(24f) }
    private val text = settings.addString("Text", "Hello World!")
    private val color = settings.addColor("Color", Color.WHITE, true)

    override fun onRenderHud(delta: Float, renderer: Renderer.State) {
        renderer.matrixStack.push()
        renderer.matrixStack.model().translate(100f, 100f, 0f).scale(100f)
        renderer.buffers.uniforms.roundedCutoff.set(1f)
        draw(renderer.buffers, renderer.matrixStack, color.value)
        renderer.matrixStack.pop()

        font.drawString(renderer.matrixStack, text.value, 100f, 0f, color.value)
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
