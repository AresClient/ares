package org.aresclient.ares.api.nrender.world

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import org.aresclient.ares.api.nrender.AresRenderLayers
import org.aresclient.ares.api.nrender.Drawer
import org.aresclient.ares.api.nrender.LineDrawer
import org.aresclient.ares.api.util.Color

object WorldDrawer: Drawer() {
    private val quadBuffer = indexedBuffers.getBuffer(AresRenderLayers.QUAD_NO_DEPTH)
    private val linesBuffer = indexedBuffers.getBuffer(AresRenderLayers.LINES_NO_DEPTH)

    fun fillQuad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color: Color) {
        fillQuad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color)
    }

    fun fillQuad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color1: Color, color2: Color, color3: Color, color4: Color) = quadBuffer.use {
        quad(
            begin().floats(x1, y1, z1).color(color1).next(),
            begin().floats(x2, y2, z2).color(color2).next(),
            begin().floats(x3, y3, z3).color(color3).next(),
            begin().floats(x4, y4, z4).color(color4).next()
        )
    }

    fun fillBox(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, vararg excludedSides: Direction) = quadBuffer.use {
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

    fun fillBox(box: Box, color: Color, vararg excludedSides: Direction) = fillBox(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, excludedSides = excludedSides)

    fun drawLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color1: Color, color2: Color, w1: Float, w2: Float) {
       LineDrawer.draw3dLine(linesBuffer, x1, y1, z1, x2, y2, z2, color1, color2, w1, w2)
    }

    fun drawLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color, width: Float) {
        drawLine(x1, y1, z1, x2, y2, z2, color, color, width, width)
    }

    fun outlineBox(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, width: Float) {
        // bottom quad
        drawLine(minX, minY, minZ, maxX, minY, minZ, color, width)
        drawLine(maxX, minY, maxZ, maxX, minY, minZ, color, width)
        drawLine(maxX, minY, maxZ, minX, minY, maxZ, color, width)
        drawLine(minX, minY, minZ, minX, minY, maxZ, color, width)

        // top quad
        drawLine(minX, maxY, minZ, maxX, maxY, minZ, color, width)
        drawLine(maxX, maxY, maxZ, maxX, maxY, minZ, color, width)
        drawLine(maxX, maxY, maxZ, minX, maxY, maxZ, color, width)
        drawLine(minX, maxY, minZ, minX, maxY, maxZ, color, width)

        // sides
        drawLine(minX, minY, minZ, minX, maxY, minZ, color, width)
        drawLine(maxX, minY, minZ, maxX, maxY, minZ, color, width)
        drawLine(maxX, minY, maxZ, maxX, maxY, maxZ, color, width)
        drawLine(minX, minY, maxZ, minX, maxY, maxZ, color, width)
    }

    fun outlineBox(box: Box, color: Color, width: Float) {
        outlineBox(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, width)
    }
}
