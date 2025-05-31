package org.aresclient.ares.api.nrender.world

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.AresRenderLayers
import org.aresclient.ares.api.util.Color
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import kotlin.math.max

object WorldDrawer: Wrapper {
    private val buffers = IndexedBufferProvider()

    object Fill {
        private val buffer = buffers.getBuffer(AresRenderLayers.QUAD_NO_DEPTH)

        fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color: Color) {
            quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color)
        }

        fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color1: Color, color2: Color, color3: Color, color4: Color) = buffer.use {
            quad(
                begin().floats(x1, y1, z1).color(color1).next(),
                begin().floats(x2, y2, z2).color(color2).next(),
                begin().floats(x3, y3, z3).color(color3).next(),
                begin().floats(x4, y4, z4).color(color4).next()
            )
        }

        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, vararg excludedSides: Direction) = buffer.use {
            val i = begin().floats(minX, minY, minZ).color(color).next()
            val ix = begin().floats(maxX, minY, minZ).color(color).next()
            val ixz = begin().floats(maxX, minY, maxZ).color(color).next()
            val iz = begin().floats(minX, minY, maxZ).color(color).next()
            val iy = begin().floats(minX, maxY, minZ).color(color).next()
            val ixy = begin().floats(maxX, maxY, minZ).color(color).next()
            val ixyz = begin().floats(maxX, maxY, maxZ).color(color).next()
            val iyz = begin().floats(minX, maxY, maxZ).color(color).next()

            if(!excludedSides.contains(Direction.DOWN))    quad(i, ix, ixz, iz)
            if(!excludedSides.contains(Direction.WEST))    quad(iz, iyz, iy, i)
            if(!excludedSides.contains(Direction.EAST))    quad(ix, ixy, ixyz, ixz)
            if(!excludedSides.contains(Direction.NORTH))   quad(i, iy, ixy, ix)
            if(!excludedSides.contains(Direction.SOUTH))   quad(ixz, ixyz, iyz, iz)
            if(!excludedSides.contains(Direction.UP))      quad(iyz, ixyz, ixy, iy)
        }

        fun box(box: Box, color: Color, vararg excludedSides: Direction) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, excludedSides = excludedSides)
    }

    object Lines {
        private const val AA_RADIUS = 2f

        private var buffer = buffers.getBuffer(AresRenderLayers.LINES_NO_DEPTH)
        private val mvp = Matrix4f()

        // cpu lines, we convert lines to quads
        fun line(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color1: Color, color2: Color, w1: Float, w2: Float) = buffer.use {
            val viewportWidth = MC.framebuffer.textureWidth.toFloat()
            val viewportHeight = MC.framebuffer.textureHeight.toFloat()
            val viewportSize = Vector2f(viewportWidth, viewportHeight)
            val aspectRatio = viewportWidth / viewportHeight

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

        fun line(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color, width: Float) {
            line(x1, y1, z1, x2, y2, z2, color, color, width, width)
        }

        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, width: Float) {
            // bottom quad
            line(minX, minY, minZ, maxX, minY, minZ, color, width)
            line(maxX, minY, maxZ, maxX, minY, minZ, color, width)
            line(maxX, minY, maxZ, minX, minY, maxZ, color, width)
            line(minX, minY, minZ, minX, minY, maxZ, color, width)

            // top quad
            line(minX, maxY, minZ, maxX, maxY, minZ, color, width)
            line(maxX, maxY, maxZ, maxX, maxY, minZ, color, width)
            line(maxX, maxY, maxZ, minX, maxY, maxZ, color, width)
            line(minX, maxY, minZ, minX, maxY, maxZ, color, width)

            // sides
            line(minX, minY, minZ, minX, maxY, minZ, color, width)
            line(maxX, minY, minZ, maxX, maxY, minZ, color, width)
            line(maxX, minY, maxZ, maxX, maxY, maxZ, color, width)
            line(minX, minY, maxZ, minX, maxY, maxZ, color, width)
        }

        fun box(box: Box, color: Color, width: Float) {
            box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, width)
        }
    }

    fun draw() {
        buffers.draw()
    }
}
