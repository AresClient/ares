package org.aresclient.ares.impl

import org.aresclient.ares.api.Camera
import org.aresclient.ares.api.IMatrixStack
import org.aresclient.ares.api.IRenderer

object Renderer: IRenderer {
    private val renderStack = MatrixStack()

    override fun getRenderStack(): IMatrixStack {
        return renderStack
    }

    override fun getCamera(): Camera {
        return CameraWrapper
    }
}