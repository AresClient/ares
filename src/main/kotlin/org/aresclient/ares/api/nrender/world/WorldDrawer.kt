package org.aresclient.ares.api.nrender.world

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.nrender.AresRenderLayers
import org.aresclient.ares.api.nrender.Drawer
import org.aresclient.ares.api.nrender.Lines
import org.aresclient.ares.api.util.Color
import org.joml.Vector3f

object WorldDrawer: Drawer() {
    private val allDirections = arrayOf(true, true, true, true, true,  true)
    private val quadBuffer = indexedBuffers.getBuffer(AresRenderLayers.QUAD_NO_DEPTH)
    private val linesBuffer = indexedBuffers.getBuffer(AresRenderLayers.LINES_NO_DEPTH)
    private val lines = mutableListOf<Lines.Line3d>()

    fun fillQuad(pos1: Vec3d, pos2: Vec3d, pos3: Vec3d, pos4: Vec3d, color: Color) {
        fillQuad(pos1.x.toFloat(), pos1.y.toFloat(), pos1.z.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), pos2.z.toFloat(), pos3.x.toFloat(), pos3.y.toFloat(), pos3.z.toFloat(), pos4.x.toFloat(), pos4.y.toFloat(), pos4.z.toFloat(), color)
    }

    fun fillQuad(pos1: Vector3f, pos2: Vector3f, pos3: Vector3f, pos4: Vector3f, color: Color) {
        fillQuad(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z, color)
    }

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

    fun fillBox(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, directions: Array<Boolean> = allDirections) = quadBuffer.use {
        val i = begin().floats(minX, minY, minZ).color(color).next()
        val ix = begin().floats(maxX, minY, minZ).color(color).next()
        val ixz = begin().floats(maxX, minY, maxZ).color(color).next()
        val iz = begin().floats(minX, minY, maxZ).color(color).next()
        val iy = begin().floats(minX, maxY, minZ).color(color).next()
        val ixy = begin().floats(maxX, maxY, minZ).color(color).next()
        val ixyz = begin().floats(maxX, maxY, maxZ).color(color).next()
        val iyz = begin().floats(minX, maxY, maxZ).color(color).next()

        if(directions[Direction.DOWN.ordinal]) quad(i, ix, ixz, iz)
        if(directions[Direction.UP.ordinal]) quad(iyz, ixyz, ixy, iy)
        if(directions[Direction.NORTH.ordinal]) quad(i, iy, ixy, ix)
        if(directions[Direction.SOUTH.ordinal]) quad(ixz, ixyz, iyz, iz)
        if(directions[Direction.WEST.ordinal]) quad(iz, iyz, iy, i)
        if(directions[Direction.EAST.ordinal]) quad(ix, ixy, ixyz, ixz)
    }

    fun fillBox(box: Box, color: Color, directions: Array<Boolean> = allDirections) = fillBox(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, directions)

    fun drawLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color1: Color, color2: Color, w1: Float, w2: Float) {
       lines.add(Lines.Line3d(x1, y1, z1, x2, y2, z2, color1, color2, w1, w2))
    }

    fun drawLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color, width: Float) {
        drawLine(x1, y1, z1, x2, y2, z2, color, color, width, width)
    }


    fun outlineQuad(pos1: Vec3d, pos2: Vec3d, pos3: Vec3d, pos4: Vec3d, color: Color, width: Float) {
        outlineQuad(pos1.x.toFloat(), pos1.y.toFloat(), pos1.z.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), pos2.z.toFloat(), pos3.x.toFloat(), pos3.y.toFloat(), pos3.z.toFloat(), pos4.x.toFloat(), pos4.y.toFloat(), pos4.z.toFloat(), color, width)
    }

    fun outlineQuad(pos1: Vector3f, pos2: Vector3f, pos3: Vector3f, pos4: Vector3f, color: Color, width: Float) {
        outlineQuad(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z, color, width)
    }

    fun outlineQuad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color: Color, width: Float) {
        outlineQuad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color, width, width, width, width)
    }

    fun outlineQuad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color1: Color, color2: Color, color3: Color, color4: Color, w1: Float, w2: Float, w3: Float, w4: Float) = quadBuffer.use {
        drawLine(x1, y1, z1, x2, y2, z2, color1, color2, w1, w2)
        drawLine(x2, y2, z2, x3, y3, z3, color2, color3, w2, w3)
        drawLine(x3, y3, z3, x4, y4, z4, color3, color4, w3, w4)
        drawLine(x4, y4, z4, x1, y1, z1, color4, color1, w4, w1)
    }

    fun outlineBox(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, width: Float, directions: Array<Boolean> = allDirections) {
        // bottom quad
        if(directions[Direction.DOWN.ordinal] && directions[Direction.NORTH.ordinal]) drawLine(minX, minY, minZ, maxX, minY, minZ, color, width)
        if(directions[Direction.DOWN.ordinal] && directions[Direction.EAST.ordinal])  drawLine(maxX, minY, maxZ, maxX, minY, minZ, color, width)
        if(directions[Direction.DOWN.ordinal] && directions[Direction.SOUTH.ordinal]) drawLine(maxX, minY, maxZ, minX, minY, maxZ, color, width)
        if(directions[Direction.DOWN.ordinal] && directions[Direction.WEST.ordinal])  drawLine(minX, minY, minZ, minX, minY, maxZ, color, width)

        // top quad
        if(directions[Direction.UP.ordinal] && directions[Direction.NORTH.ordinal]) drawLine(minX, maxY, minZ, maxX, maxY, minZ, color, width)
        if(directions[Direction.UP.ordinal] && directions[Direction.EAST.ordinal])  drawLine(maxX, maxY, maxZ, maxX, maxY, minZ, color, width)
        if(directions[Direction.UP.ordinal] && directions[Direction.SOUTH.ordinal]) drawLine(maxX, maxY, maxZ, minX, maxY, maxZ, color, width)
        if(directions[Direction.UP.ordinal] && directions[Direction.WEST.ordinal])  drawLine(minX, maxY, minZ, minX, maxY, maxZ, color, width)

        // sides
        if(directions[Direction.NORTH.ordinal] && directions[Direction.WEST.ordinal]) drawLine(minX, minY, minZ, minX, maxY, minZ, color, width)
        if(directions[Direction.NORTH.ordinal] && directions[Direction.EAST.ordinal]) drawLine(maxX, minY, minZ, maxX, maxY, minZ, color, width)
        if(directions[Direction.SOUTH.ordinal] && directions[Direction.EAST.ordinal]) drawLine(maxX, minY, maxZ, maxX, maxY, maxZ, color, width)
        if(directions[Direction.SOUTH.ordinal] && directions[Direction.WEST.ordinal]) drawLine(minX, minY, maxZ, minX, maxY, maxZ, color, width)
    }

    fun outlineBox(box: Box, color: Color, width: Float, directions: Array<Boolean> = allDirections) {
        outlineBox(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, width, directions)
    }

    override fun draw() {
        Lines.draw(lines, linesBuffer)
        lines.clear()
        super.draw()
    }
}
