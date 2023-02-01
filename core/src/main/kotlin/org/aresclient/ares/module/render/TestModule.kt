package org.aresclient.ares.module.render

import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.Color

// a simple mesh test module
object TestModule: Module("Test", "A simple test module", Category.RENDER, enabled = true) {
    private var text = settings.string("Text", "Hello World!")

    override fun onRenderHud(delta: Float) {
        MC.getTextRenderer().drawText(text.value, 0f, 0f, Color.WHITE)
    }
}
