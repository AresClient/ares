package org.aresclient.ares.global

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.api.math.Box
import net.meshmc.mesh.api.math.Vec3d
import net.meshmc.mesh.api.math.Vec3f
import net.meshmc.mesh.event.events.render.RenderEvent
import net.meshmc.mesh.util.math.Facing
import net.meshmc.mesh.util.render.Color
import net.meshmc.mesh.util.render.Vertex
import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer

object RenderGlobal: Global("Render Global") {
    data class Event(val delta: Float, val buffers: Buffers)
    data class Buffers(val positionColor: Buffer, val lines: Buffer)

    private val buffers by lazy {
        Buffers(
            Buffer.createDynamic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR),
            Buffer.createDynamic(Shader.LINES, VertexFormat.LINES).lines()
        )
    }

    @field:EventHandler
    private val renderWorldEvent = EventListener<RenderEvent.World> { e ->
        buffers.positionColor.reset()
        buffers.lines.reset()

        val event = Ares.MESH.eventManager.post(Event(e.tickDelta, buffers))
        Ares.MODULES.forEach { it.renderWorld(event) }

        Renderer.render3d(buffers.positionColor::draw, false)
        Renderer.render3d(buffers.lines::draw)
    }
    
    object Fill {
        fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color1: Color, color2: Color, color3: Color, color4: Color) {
            buffers.positionColor.indicesOffset(
                0, 1, 2,
                2, 3, 0
            )

            buffers.positionColor.vertices(
                x1, y1, z1, color1.red, color1.green, color1.blue, color1.alpha,
                x2, y2, z2, color2.red, color2.green, color2.blue, color2.alpha,
                x3, y3, z3, color3.red, color3.green, color3.blue, color3.alpha,
                x4, y4, z4, color4.red, color4.green, color4.blue, color4.alpha
            )
        }

        fun quad(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, x3: Double, y3: Double, z3: Double, x4: Double, y4: Double, z4: Double, color1: Color, color2: Color, color3: Color, color4: Color) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x3.toFloat(), y3.toFloat(), z3.toFloat(), x4.toFloat(), y4.toFloat(), z4.toFloat(), color1, color2, color3, color4)
        fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color: Color) = quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color)
        fun quad(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, x3: Double, y3: Double, z3: Double, x4: Double, y4: Double, z4: Double, color: Color) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x3.toFloat(), y3.toFloat(), z3.toFloat(), x4.toFloat(), y4.toFloat(), z4.toFloat(), color, color, color, color)
        fun quad(pos1: Vec3f, pos2: Vec3f, pos3: Vec3f, pos4: Vec3f, color1: Color, color2: Color, color3: Color, color4: Color) = quad(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z, color1, color2, color3, color4)
        fun quad(pos1: Vec3d, pos2: Vec3d, pos3: Vec3d, pos4: Vec3d, color1: Color, color2: Color, color3: Color, color4: Color) = quad(pos1.x.toFloat(), pos1.y.toFloat(), pos1.z.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), pos2.z.toFloat(), pos3.x.toFloat(), pos3.y.toFloat(), pos3.z.toFloat(), pos4.x.toFloat(), pos4.y.toFloat(), pos4.z.toFloat(), color1, color2, color3, color4)
        fun quad(vertex1: Vertex, vertex2: Vertex, vertex3: Vertex, vertex4: Vertex) = quad(vertex1.x.toFloat(), vertex1.y.toFloat(), vertex1.z.toFloat(), vertex2.x.toFloat(), vertex2.y.toFloat(), vertex2.z.toFloat(), vertex3.x.toFloat(), vertex3.y.toFloat(), vertex3.z.toFloat(), vertex4.x.toFloat(), vertex4.y.toFloat(), vertex4.z.toFloat(), vertex1.color, vertex2.color, vertex3.color, vertex4.color)
        fun quad(pos1: Vec3f, pos2: Vec3f, pos3: Vec3f, pos4: Vec3f, color: Color) = quad(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z, color, color, color, color)
        fun quad(pos1: Vec3d, pos2: Vec3d, pos3: Vec3d, pos4: Vec3d, color: Color) = quad(pos1.x.toFloat(), pos1.y.toFloat(), pos1.z.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), pos2.z.toFloat(), pos3.x.toFloat(), pos3.y.toFloat(), pos3.z.toFloat(), pos4.x.toFloat(), pos4.y.toFloat(), pos4.z.toFloat(), color, color, color, color)
        fun quadVertical(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color1: Color, color2: Color) = quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color1, color2, color2, color1)
        fun quadVertical(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color1: Color, color2: Color) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x1.toFloat(), y2.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x2.toFloat(), y1.toFloat(), z2.toFloat(), color1, color2, color2, color1)
        fun quadVertical(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color) = quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color, color, color, color)
        fun quadVertical(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color: Color) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x1.toFloat(), y2.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x2.toFloat(), y1.toFloat(), z2.toFloat(), color, color, color, color)
        fun quadHorizontal(x1: Float, y: Float, z1: Float, x2: Float, z2: Float, color: Color) = quad(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, color, color, color, color)
        fun quadHorizontal(x1: Double, y: Double, z1: Double, x2: Double, z2: Double, color: Color) = quad(x1.toFloat(), y.toFloat(), z1.toFloat(), x1.toFloat(), y.toFloat(), z2.toFloat(), x2.toFloat(), y.toFloat(), z2.toFloat(), x2.toFloat(), y.toFloat(), z1.toFloat(), color, color, color, color)

        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, vararg excludedSides: Facing) {
            if(excludedSides.isEmpty()) {
                buffers.positionColor.indicesOffset(
                    0,1,2, 2,3,0,
                    3,7,4, 4,0,3,
                    1,5,6, 6,2,1,
                    0,4,5, 5,1,0,
                    2,6,7, 7,3,2,
                    7,6,5, 5,4,7
                )
            } else {
                if(!excludedSides.contains(Facing.DOWN))    buffers.positionColor.indicesOffset(0,1,2, 2,3,0)
                if(!excludedSides.contains(Facing.WEST))    buffers.positionColor.indicesOffset(3,7,4, 4,0,3)
                if(!excludedSides.contains(Facing.EAST))    buffers.positionColor.indicesOffset(1,5,6, 6,2,1)
                if(!excludedSides.contains(Facing.NORTH))   buffers.positionColor.indicesOffset(0,4,5, 5,1,0)
                if(!excludedSides.contains(Facing.SOUTH))   buffers.positionColor.indicesOffset(2,6,7, 7,3,2)
                if(!excludedSides.contains(Facing.UP))      buffers.positionColor.indicesOffset(7,6,5, 5,4,7)
            }

            buffers.positionColor.vertices(
                minX, minY, minZ, color.red, color.green, color.blue, color.alpha,
                maxX, minY, minZ, colorX.red, colorX.green, colorX.blue, colorX.alpha,
                maxX, minY, maxZ, colorXZ.red, colorXZ.green, colorXZ.blue, colorXZ.alpha,
                minX, minY, maxZ, colorZ.red, colorZ.green, colorZ.blue, colorZ.alpha,
                minX, maxY, minZ, colorY.red, colorY.green, colorY.blue, colorY.alpha,
                maxX, maxY, minZ, colorXY.red, colorXY.green, colorXY.blue, colorXY.alpha,
                maxX, maxY, maxZ, colorXYZ.red, colorXYZ.green, colorXYZ.blue, colorXYZ.alpha,
                minX, maxY, maxZ, colorYZ.red, colorYZ.green, colorYZ.blue, colorYZ.alpha,
            )
        }

        fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, vararg excludedSides: Facing) = box(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(), color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, excludedSides = excludedSides)
        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, colorBottom: Color, colorTop: Color, vararg excludedSides: Facing) = box(minX, minY, minZ, maxX, maxY, maxZ, colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, excludedSides = excludedSides)
        fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, colorBottom: Color, colorTop: Color, vararg excludedSides: Facing) = box(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(), colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, excludedSides = excludedSides)
        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, vararg excludedSides: Facing) = box(minX, minY, minZ, maxX, maxY, maxZ, color, color, color, color, color, color, color, color, excludedSides = excludedSides)
        fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, color: Color, vararg excludedSides: Facing) = box(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(), color, color, color, color, color, color, color, color, excludedSides = excludedSides)
        fun box(minPos: Vec3f, maxPos: Vec3f, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, vararg excludedSides: Facing) = box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z, color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, excludedSides = excludedSides)
        fun box(minPos: Vec3d, maxPos: Vec3d, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, vararg excludedSides: Facing) = box(minPos.x.toFloat(), minPos.y.toFloat(), minPos.z.toFloat(), maxPos.x.toFloat(), maxPos.y.toFloat(), maxPos.z.toFloat(), color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, excludedSides = excludedSides)
        fun box(minPos: Vec3f, maxPos: Vec3f, colorBottom: Color, colorTop: Color, vararg excludedSides: Facing) = box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z, colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, excludedSides = excludedSides)
        fun box(minPos: Vec3d, maxPos: Vec3d, colorBottom: Color, colorTop: Color, vararg excludedSides: Facing) = box(minPos.x.toFloat(), minPos.y.toFloat(), minPos.z.toFloat(), maxPos.x.toFloat(), maxPos.y.toFloat(), maxPos.z.toFloat(), colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, excludedSides = excludedSides)
        fun box(minPos: Vec3f, maxPos: Vec3f, color: Color, vararg excludedSides: Facing) = box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z, color, color, color, color, color, color, color, color, excludedSides = excludedSides)
        fun box(minPos: Vec3d, maxPos: Vec3d, color: Color, vararg excludedSides: Facing) = box(minPos.x.toFloat(), minPos.y.toFloat(), minPos.z.toFloat(), maxPos.x.toFloat(), maxPos.y.toFloat(), maxPos.z.toFloat(), color, color, color, color, color, color, color, color, excludedSides = excludedSides)
        fun box(box: Box, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, vararg excludedSides: Facing) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, excludedSides = excludedSides)
        fun box(box: Box, colorBottom: Color, colorTop: Color, vararg excludedSides: Facing) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, excludedSides = excludedSides)
        fun box(box: Box, color: Color, vararg excludedSides: Facing) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, color, color, color, color, color, color, color, excludedSides = excludedSides)
    }
    
    object Lines {
        fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color1: Color, color2: Color, color3: Color, color4: Color, w1: Float, w2: Float, w3: Float, w4: Float) {
            buffers.lines.indicesOffset(
                0, 1,
                1, 2,
                2, 3,
                3, 0
            )

            buffers.lines.vertices(
                x1, y1, z1, w1, color1.red, color1.green, color1.blue, color1.alpha,
                x2, y2, z2, w2, color2.red, color2.green, color2.blue, color2.alpha,
                x3, y3, z3, w3, color3.red, color3.green, color3.blue, color3.alpha,
                x4, y4, z4, w4, color4.red, color4.green, color4.blue, color4.alpha
            )
        }

        fun quad(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, x3: Double, y3: Double, z3: Double, x4: Double, y4: Double, z4: Double, color1: Color, color2: Color, color3: Color, color4: Color, w1: Float, w2: Float, w3: Float, w4: Float) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x3.toFloat(), y3.toFloat(), z3.toFloat(), x4.toFloat(), y4.toFloat(), z4.toFloat(), color1, color2, color3, color4, w1, w2, w3, w4)
        fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float, color: Color, w: Float) = quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color, w, w, w, w)
        fun quad(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, x3: Double, y3: Double, z3: Double, x4: Double, y4: Double, z4: Double, color: Color, w: Float) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x3.toFloat(), y3.toFloat(), z3.toFloat(), x4.toFloat(), y4.toFloat(), z4.toFloat(), color, color, color, color, w, w, w, w)
        fun quadVertical(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color1: Color, color2: Color, w1: Float, w2: Float) = quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color1, color2, color2, color1, w1, w2, w2, w1)
        fun quadVertical(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color1: Color, color2: Color, w1: Float, w2: Float) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x1.toFloat(), y2.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x2.toFloat(), y1.toFloat(), z2.toFloat(), color1, color2, color2, color1, w1, w2, w2, w1)
        fun quadVertical(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Color, w: Float) = quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color, color, color, color, w, w, w, w)
        fun quadVertical(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color: Color, w: Float) = quad(x1.toFloat(), y1.toFloat(), z1.toFloat(), x1.toFloat(), y2.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), x2.toFloat(), y1.toFloat(), z2.toFloat(), color, color, color, color, w, w, w, w)
        fun quadHorizontal(x1: Float, y: Float, z1: Float, x2: Float, z2: Float, color: Color, w: Float) = quad(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, color, color, color, color, w, w, w, w)
        fun quadHorizontal(x1: Double, y: Double, z1: Double, x2: Double, z2: Double, color: Color, w: Float) = quad(x1.toFloat(), y.toFloat(), z1.toFloat(), x1.toFloat(), y.toFloat(), z2.toFloat(), x2.toFloat(), y.toFloat(), z2.toFloat(), x2.toFloat(), y.toFloat(), z1.toFloat(), color, color, color, color, w, w, w, w)

        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, width: Float, vararg excludedSides: Facing) {
            if(excludedSides.isEmpty()) {
                buffers.lines.indicesOffset(
                    0, 4,
                    1, 5,
                    2, 6,
                    3, 7,
                    // bottom
                    0, 1,
                    2, 3,
                    0, 2,
                    1, 3,
                    // top
                    4, 5,
                    6, 7,
                    4, 6,
                    5, 7
                )
            } else {
                if(!excludedSides.contains(Facing.WEST) && !excludedSides.contains(Facing.NORTH)) buffers.lines.indicesOffset(0, 4)
                if(!excludedSides.contains(Facing.WEST) && !excludedSides.contains(Facing.SOUTH)) buffers.lines.indicesOffset(1, 5)
                if(!excludedSides.contains(Facing.EAST) && !excludedSides.contains(Facing.NORTH)) buffers.lines.indicesOffset(2, 6)
                if(!excludedSides.contains(Facing.EAST) && !excludedSides.contains(Facing.SOUTH)) buffers.lines.indicesOffset(3, 7)
                // bottom
                if(!excludedSides.contains(Facing.WEST) && !excludedSides.contains(Facing.DOWN)) buffers.lines.indicesOffset(0, 1)
                if(!excludedSides.contains(Facing.EAST) && !excludedSides.contains(Facing.DOWN)) buffers.lines.indicesOffset(2, 3)
                if(!excludedSides.contains(Facing.NORTH) && !excludedSides.contains(Facing.DOWN)) buffers.lines.indicesOffset(0, 2)
                if(!excludedSides.contains(Facing.SOUTH) && !excludedSides.contains(Facing.DOWN)) buffers.lines.indicesOffset(1, 3)
                // top
                if(!excludedSides.contains(Facing.WEST) && !excludedSides.contains(Facing.UP)) buffers.lines.indicesOffset(4, 5)
                if(!excludedSides.contains(Facing.EAST) && !excludedSides.contains(Facing.UP)) buffers.lines.indicesOffset(6, 7)
                if(!excludedSides.contains(Facing.NORTH) && !excludedSides.contains(Facing.UP)) buffers.lines.indicesOffset(4, 6)
                if(!excludedSides.contains(Facing.SOUTH) && !excludedSides.contains(Facing.UP)) buffers.lines.indicesOffset(5, 7)
            }

            buffers.lines.vertices(
                minX, minY, minZ, width, color.red, color.green, color.blue, color.alpha,
                minX, minY, maxZ, width, colorZ.red, colorZ.green, colorZ.blue, colorZ.alpha,
                maxX, minY, minZ, width, colorX.red, colorX.green, colorX.blue, colorX.alpha,
                maxX, minY, maxZ, width, colorXZ.red, colorXZ.green, colorXZ.blue, colorXZ.alpha,
                minX, maxY, minZ, width, colorY.red, colorY.green, colorY.blue, colorY.alpha,
                minX, maxY, maxZ, width, colorYZ.red, colorYZ.green, colorYZ.blue, colorYZ.alpha,
                maxX, maxY, minZ, width, colorXY.red, colorXY.green, colorXY.blue, colorXY.alpha,
                maxX, maxY, maxZ, width, colorXYZ.red, colorXYZ.green, colorXYZ.blue, colorXYZ.alpha
            )
        }

        fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, weight: Float, vararg excludedSides: Facing) = box(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(), color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, weight, excludedSides = excludedSides)
        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, colorBottom: Color, colorTop: Color, weight: Float, vararg excludedSides: Facing) = box(minX, minY, minZ, maxX, maxY, maxZ, colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, weight, excludedSides = excludedSides)
        fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, colorBottom: Color, colorTop: Color, weight: Float, vararg excludedSides: Facing) = box(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(), colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, weight, excludedSides = excludedSides)
        fun box(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, color: Color, weight: Float, vararg excludedSides: Facing) = box(minX, minY, minZ, maxX, maxY, maxZ, color, color, color, color, color, color, color, color, weight, excludedSides = excludedSides)
        fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, color: Color, weight: Float, vararg excludedSides: Facing) = box(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(), color, color, color, color, color, color, color, color, weight, excludedSides = excludedSides)
        fun box(minPos: Vec3f, maxPos: Vec3f, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, weight: Float, vararg excludedSides: Facing) = box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z, color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, weight, excludedSides = excludedSides)
        fun box(minPos: Vec3d, maxPos: Vec3d, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, weight: Float, vararg excludedSides: Facing) = box(minPos.x.toFloat(), minPos.y.toFloat(), minPos.z.toFloat(), maxPos.x.toFloat(), maxPos.y.toFloat(), maxPos.z.toFloat(), color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, weight, excludedSides = excludedSides)
        fun box(minPos: Vec3f, maxPos: Vec3f, colorBottom: Color, colorTop: Color, weight: Float, vararg excludedSides: Facing) = box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z, colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, weight, excludedSides = excludedSides)
        fun box(minPos: Vec3d, maxPos: Vec3d, colorBottom: Color, colorTop: Color, weight: Float, vararg excludedSides: Facing) = box(minPos.x.toFloat(), minPos.y.toFloat(), minPos.z.toFloat(), maxPos.x.toFloat(), maxPos.y.toFloat(), maxPos.z.toFloat(), colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, weight, excludedSides = excludedSides)
        fun box(minPos: Vec3f, maxPos: Vec3f, color: Color, weight: Float, vararg excludedSides: Facing) = box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z, color, color, color, color, color, color, color, color, weight, excludedSides = excludedSides)
        fun box(minPos: Vec3d, maxPos: Vec3d, color: Color, weight: Float, vararg excludedSides: Facing) = box(minPos.x.toFloat(), minPos.y.toFloat(), minPos.z.toFloat(), maxPos.x.toFloat(), maxPos.y.toFloat(), maxPos.z.toFloat(), color, color, color, color, color, color, color, color, weight, excludedSides = excludedSides)
        fun box(box: Box, color: Color, colorX: Color, colorXZ: Color, colorZ: Color, colorY: Color, colorXY: Color, colorXYZ: Color, colorYZ: Color, weight: Float, vararg excludedSides: Facing) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, colorX, colorXZ, colorZ, colorY, colorXY, colorXYZ, colorYZ, weight, excludedSides = excludedSides)
        fun box(box: Box, colorBottom: Color, colorTop: Color, weight: Float, vararg excludedSides: Facing) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), colorBottom, colorBottom, colorBottom, colorBottom, colorTop, colorTop, colorTop, colorTop, weight, excludedSides = excludedSides)
        fun box(box: Box, color: Color, weight: Float, vararg excludedSides: Facing) = box(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), color, color, color, color, color, color, color, color, weight, excludedSides = excludedSides)
    }
}
