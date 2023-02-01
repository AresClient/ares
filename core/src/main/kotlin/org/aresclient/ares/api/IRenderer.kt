package org.aresclient.ares.api

interface IRenderer {
    fun getRenderStack(): IMatrixStack

    fun getCamera(): Camera
}