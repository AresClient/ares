package org.aresclient.ares.api

import org.joml.Matrix4f

interface IMatrixStack {
    fun push()

    fun pop()

    fun scale(x: Double, y: Double, z: Double)

    fun translate(x: Double, y: Double, z: Double)

    fun rotate(angle: Float, x: Float, y: Float, z: Float)

    fun getProjectionMatrix(): Matrix4f

    fun getModelMatrix(): Matrix4f

    fun translate(camera: Camera) {
        translate(-camera.x, -camera.y, -camera.z)
    }
}