package org.aresclient.ares.impl

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.cos
import kotlin.math.sin

class MatrixStack: org.aresclient.ares.api.IMatrixStack {
    private var matrixStack = MatrixStack()

    override fun push() {
        matrixStack.push()
    }

    override fun pop() {
        matrixStack.pop()
    }

    override fun scale(x: Double, y: Double, z: Double) {
        matrixStack.scale(x.toFloat(), y.toFloat(), z.toFloat())
    }

    override fun translate(x: Double, y: Double, z: Double) {
        matrixStack.translate(x, y, z)
    }

    override fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        var xa = (x * angle) * 0.017453292F
        var ya = (y * angle) * 0.017453292F
        var za = (z * angle) * 0.017453292F

        val xb = sin(0.5F * xa)
        val xc = cos(0.5F * xa)
        val yb = sin(0.5F * ya)
        val yc = cos(0.5F * ya)
        val zb = sin(0.5F * za)
        val zc = cos(0.5F * za)

        xa = xb * yc * zc + xc * yb * zb
        ya = xc * yb * zc - xb * yc * zb
        za = xb * yb * zc + xc * yc * zb
        val w = xc * yc * zc - xb * yb * zb

        matrixStack.multiply(Quaternionf(xa, ya, za, w))
    }

    override fun getProjectionMatrix(): Matrix4f {
        return RenderSystem.getProjectionMatrix()
    }

    override fun getModelMatrix(): Matrix4f {
        return matrixStack.peek().positionMatrix
    }

    fun setMatrixStack(matrixStack: MatrixStack) {
        this.matrixStack = matrixStack
    }

    fun getMatrixStack(): MatrixStack? {
        return matrixStack
    }
}