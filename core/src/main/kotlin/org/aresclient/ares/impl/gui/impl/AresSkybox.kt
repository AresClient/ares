package org.aresclient.ares.impl.gui.impl

import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.SkyBox

object AresSkybox {
    private val SKYBOX =
        SkyBox("/assets/ares/textures/panorama/2b2t")
    private val SKYBOX_STACK = MatrixStack()
        .also { it.model().rotate(Math.toRadians(45.0).toFloat(), 0f, 1f, 0f) }

    fun update(width: Float, height: Float) {
        SKYBOX_STACK.projection().setPerspective(1.5f, width / height, 0.1f, 2f)
    }

    fun draw(delta: Float) {
        SKYBOX.render(SKYBOX_STACK)
        SKYBOX_STACK.model().rotate((0.0002 * delta).toFloat(), 0f, 1f, 0f)
    }
}