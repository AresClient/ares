package org.aresclient.ares.api.nrender.drawer

import com.mojang.blaze3d.systems.RenderSystem
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.buffer.IndexedBuffer
import org.aresclient.ares.api.util.Color
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import kotlin.math.max

// This provides methods for drawing smooth lines with any thickness by extending them into quads, and aliasing in fragment shader
object Lines: Wrapper {
    private const val AA_RADIUS = 2f
    private val mvp = Matrix4f()

    fun begin() {
        RenderSystem.getModelViewMatrix().mul(RenderSystem.getProjectionMatrix(), mvp)
    }

    // TODO: fix line width problems as line gets further away from camera
    fun draw3dLine(buffer: IndexedBuffer, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color1: Color, color2: Color, w1: Float, w2: Float) = buffer.use {
        val viewportSize = getViewportSize()
        val aspectRatio = viewportSize.x / viewportSize.y

        // get positions in clip space
        RenderSystem.getModelViewMatrix().mul(RenderSystem.getProjectionMatrix(), mvp)
        val clip1 = Vector4f(x1, y1, z1, 1f).mul(mvp)
        val clip2 = Vector4f(x2, y2, z2, 1f).mul(mvp)

        // get positions in normalized device coordinates
        val ndc1 = Vector2f(clip1.x, clip1.y).div(clip1.w)
        val ndc2 = Vector2f(clip2.x, clip2.y).div(clip2.w)

        // get vector of the line (also in viewport space for length calculation)
        val ndcVector = ndc2.sub(ndc1, Vector2f())
        val viewportVector = ndcVector.mul(viewportSize, Vector2f())

        // get direction and normal of vector
        val dir = Vector2f(ndcVector.x, ndcVector.y * aspectRatio).normalize() // correct for aspect ratio
        val normal = Vector2f(-dir.y, dir.x)

        // calculate distances for offsets
        val widthRadius1 = max(1f, w1) + AA_RADIUS // also width offset scalar
        val widthRadius2 = max(1f, w2) + AA_RADIUS // also width offset scalar
        val lengthRadius = viewportVector.length() / 2f + AA_RADIUS // not length offset scalar, length offset scalar is aaRadius

        // calculate offsets from line points
        val widthOffset1 = Vector2f(widthRadius1).div(viewportSize).mul(normal)
        val widthOffset2 = Vector2f(widthRadius2).div(viewportSize).mul(normal)
        val lengthOffset = Vector2f(AA_RADIUS).div(viewportSize).mul(dir)

        // combine offsets to get vertices of quad
        val pos0 = Vector4f(ndc1.sub(widthOffset1, Vector2f()).sub(lengthOffset).mul(clip1.w), clip1.z, clip1.w)
        val pos1 = Vector4f(ndc1.add(widthOffset1, Vector2f()).sub(lengthOffset).mul(clip1.w), clip1.z, clip1.w)
        val pos2 = Vector4f(ndc2.add(widthOffset2, Vector2f()).add(lengthOffset).mul(clip2.w), clip2.z, clip2.w)
        val pos3 = Vector4f(ndc2.sub(widthOffset2, Vector2f()).add(lengthOffset).mul(clip2.w), clip2.z, clip2.w)

        quad(
            begin().pos(pos0).color(color1).floats(widthRadius1, -lengthRadius, widthRadius1, lengthRadius).next(),
            begin().pos(pos1).color(color1).floats(-widthRadius1, -lengthRadius, widthRadius1, lengthRadius).next(),
            begin().pos(pos2).color(color2).floats(-widthRadius1, lengthRadius, widthRadius2, lengthRadius).next(),
            begin().pos(pos3).color(color2).floats(widthRadius1, lengthRadius, widthRadius2, lengthRadius).next(),
        )
    }

    // basically the same as draw3dLine, but forces specified z pos for calculated quad
    fun draw2dLine(buffer: IndexedBuffer, x1: Float, y1: Float, x2: Float, y2: Float, z: Float, color1: Color, color2: Color, w1: Float, w2: Float) = buffer.use {
        val viewportSize = getViewportSize()
        val aspectRatio = viewportSize.x / viewportSize.y

        // get positions in clip space
        RenderSystem.getModelViewMatrix().mul(RenderSystem.getProjectionMatrix(), mvp)
        val clip1 = Vector4f(x1, y1, z, 1f).mul(mvp)
        val clip2 = Vector4f(x2, y2, z, 1f).mul(mvp)

        // get positions in normalized device coordinates
        val ndc1 = Vector2f(clip1.x, clip1.y).div(clip1.w)
        val ndc2 = Vector2f(clip2.x, clip2.y).div(clip2.w)

        // get vector of the line (also in viewport space for length calculation)
        val ndcVector = ndc2.sub(ndc1, Vector2f())
        val viewportVector = ndcVector.mul(viewportSize, Vector2f())

        // get direction and normal of vector
        val dir = Vector2f(ndcVector.x, ndcVector.y * aspectRatio).normalize() // correct for aspect ratio
        val normal = Vector2f(-dir.y, dir.x)

        // calculate distances for offsets
        val widthRadius1 = max(1f, w1) + AA_RADIUS // also width offset scalar
        val widthRadius2 = max(1f, w2) + AA_RADIUS // also width offset scalar
        val lengthRadius = viewportVector.length() / 2f + AA_RADIUS // not length offset scalar, length offset scalar is aaRadius

        // calculate offsets from line points
        val widthOffset1 = Vector2f(widthRadius1).div(viewportSize).mul(normal)
        val widthOffset2 = Vector2f(widthRadius2).div(viewportSize).mul(normal)
        val lengthOffset = Vector2f(AA_RADIUS).div(viewportSize).mul(dir)

        // combine offsets to get vertices of quad
        val pos0 = Vector4f(ndc1.sub(widthOffset1, Vector2f()).sub(lengthOffset).mul(clip1.w), z, clip1.w)
        val pos1 = Vector4f(ndc1.add(widthOffset1, Vector2f()).sub(lengthOffset).mul(clip1.w), z, clip1.w)
        val pos2 = Vector4f(ndc2.add(widthOffset2, Vector2f()).add(lengthOffset).mul(clip2.w), z, clip2.w)
        val pos3 = Vector4f(ndc2.sub(widthOffset2, Vector2f()).add(lengthOffset).mul(clip2.w), z, clip2.w)

        quad(
            begin().pos(pos0).color(color1).floats(widthRadius1, -lengthRadius, widthRadius1, lengthRadius).next(),
            begin().pos(pos1).color(color1).floats(-widthRadius1, -lengthRadius, widthRadius1, lengthRadius).next(),
            begin().pos(pos2).color(color2).floats(-widthRadius1, lengthRadius, widthRadius2, lengthRadius).next(),
            begin().pos(pos3).color(color2).floats(widthRadius1, lengthRadius, widthRadius2, lengthRadius).next(),
        )
    }

    private fun getViewportSize(): Vector2f {
        val viewportWidth = MC.framebuffer.textureWidth.toFloat()
        val viewportHeight = MC.framebuffer.textureHeight.toFloat()
        return Vector2f(viewportWidth, viewportHeight)
    }
}
