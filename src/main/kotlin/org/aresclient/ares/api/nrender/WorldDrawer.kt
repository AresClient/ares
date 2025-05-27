package org.aresclient.ares.api.nrender

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import org.aresclient.ares.api.nrender.world.WorldBufferProvider
import org.aresclient.ares.api.util.Color
import org.joml.Matrix4f

object WorldDrawer {
    private val buffers = WorldBufferProvider()

    object Fill {
        private val buffer = buffers.getBuffer(RenderLayers.World.triangle_color)

        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, vararg excludedSides: Direction) = with(buffer) {
            vertices {
                val i = floats(minX, minY, minZ).color(color).next()
                val ix = floats(maxX, minY, minZ).color(color).next()
                val ixz = floats(maxX, minY, maxZ).color(color).next()
                val iz = floats(minX, minY, maxZ).color(color).next()
                val iy = floats(minX, maxY, minZ).color(color).next()
                val ixy = floats(maxX, maxY, minZ).color(color).next()
                val ixyz = floats(maxX, maxY, maxZ).color(color).next()
                val iyz = floats(minX, maxY, maxZ).color(color).next()

                if(!excludedSides.contains(Direction.DOWN))    quad(i, ix, ixz, iz)
                if(!excludedSides.contains(Direction.WEST))    quad(iz, iyz, iy, i)
                if(!excludedSides.contains(Direction.EAST))    quad(ix, ixy, ixyz, ixz)
                if(!excludedSides.contains(Direction.NORTH))   quad(i, iy, ixy, ix)
                if(!excludedSides.contains(Direction.SOUTH))   quad(ixz, ixyz, iyz, iz)
                if(!excludedSides.contains(Direction.UP))      quad(iyz, ixyz, ixy, iy)
            }
        }

        fun box(box: Box, color: Color, vararg excludedSides: Direction) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, excludedSides = excludedSides)
    }

    fun draw(projection: Matrix4f) {
        buffers.draw(projection)
    }
}
