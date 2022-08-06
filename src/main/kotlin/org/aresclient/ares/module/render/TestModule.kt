package org.aresclient.ares.module.render

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module

// a simple mesh test module
object TestModule: Module("Test", "A simple test module", Category.RENDER, enabled = true) {
    private var text = settings.string("Text", "Hello World!")

    override fun onRenderHud(delta: Float) {
        MC.textRenderer.drawText(text.value, 0f, 0f, Color.WHITE)
    }
}
