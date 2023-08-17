package org.aresclient.ares.impl.module.render

import org.aresclient.ares.api.module.Category
import org.aresclient.ares.api.module.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.api.util.Keys
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw

// a simple mesh test module
object TestModule: Module(Category.RENDER, "Test", "A simple test module", Defaults().setBind(Keys.Y).setEnabled(true)) {
    private val renderer by lazy { RenderHelper.getFontRenderer(24f) }
    private var text = settings.addString("Text", "Hello World!")

    override fun onRenderHud(delta: Float, buffers: Renderer.Buffers, matrixStack: MatrixStack?) {
        buffers.triangle.draw(matrixStack) {
            vertices(
                0f, 0f, 0f,       1f, 0f, 0f, 1f,
                100f, 0f, 0f,     0f, 1f, 0f, 1f,
                100f, 100f, 0f,   0f, 0f, 0f, 1f,
                0f, 100f, 0f,     1f, 0f, 1f, 1f,
            )
            indices(
                0, 1, 2,
                0, 2, 3
            )
        }

        renderer.drawString(matrixStack, text.value, 100f, 0f, Color.rainbow())
    }
}
