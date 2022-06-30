package org.aresclient.ares.module.render

import net.meshmc.mesh.api.entity.Entity
import net.meshmc.mesh.api.entity.EntityType
import net.meshmc.mesh.api.math.Box
import net.meshmc.mesh.util.math.MathHelper
import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer

object ESP: Module("ESP", "See outlines of players through walls", Category.RENDER, enabled = true) {
    private val color = settings.color("Color", Color.RED)
    private val buffer by lazy { Buffer.beginDynamic(Shader.LINES, VertexFormat.LINES).lines() }

    override fun renderWorld(delta: Float) {
        Renderer.render3d { matrixStack ->
            buffer.begin()
            MC.world.loadedEntities.forEach { entity ->
                if(entity.entityType == EntityType.COW && entity != MC.player)
                    buffer.rainbox(entity.getInterpolatedBoundingBox(delta), 2f)
            }
            buffer.end()
            buffer.draw(matrixStack)
        }
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z
        return Box.create(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }

    private fun Buffer.box(box: Box, color: Color, width: Float) {
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        indicesOffset(
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 2, 1, 3, 4, 6, 5, 7,
            0, 4, 1, 5, 2, 6, 3, 7
        )
        vertices(
            minX, minY, minZ, width, color.red, color.green, color.blue, color.alpha,
            maxX, minY, minZ, width, color.red, color.green, color.blue, color.alpha,
            minX, minY, maxZ, width, color.red, color.green, color.blue, color.alpha,
            maxX, minY, maxZ, width, color.red, color.green, color.blue, color.alpha,
            minX, maxY, minZ, width, color.red, color.green, color.blue, color.alpha,
            maxX, maxY, minZ, width, color.red, color.green, color.blue, color.alpha,
            minX, maxY, maxZ, width, color.red, color.green, color.blue, color.alpha,
            maxX, maxY, maxZ, width, color.red, color.green, color.blue, color.alpha
        )
    }

    // box but rainbow :o
    private fun Buffer.rainbox(box: Box, width: Float) {
        val color0 = rainbow(1280L)
        val color1 = rainbow(2560L)
        val color2 = rainbow(3840L)
        val color3 = rainbow(5120L)
        val color4 = rainbow(6400L)
        val color5 = rainbow(7680L)
        val color6 = rainbow(8960L)
        val color7 = rainbow(10240L)

        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        indicesOffset(
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 2, 1, 3, 4, 6, 5, 7,
            0, 4, 1, 5, 2, 6, 3, 7
        )
        vertices(
            minX, minY, minZ, width, color0.red, color0.green, color0.blue, color0.alpha,
            maxX, minY, minZ, width, color1.red, color1.green, color1.blue, color1.alpha,
            minX, minY, maxZ, width, color2.red, color2.green, color2.blue, color2.alpha,
            maxX, minY, maxZ, width, color3.red, color3.green, color3.blue, color3.alpha,
            minX, maxY, minZ, width, color4.red, color4.green, color4.blue, color4.alpha,
            maxX, maxY, minZ, width, color5.red, color5.green, color5.blue, color5.alpha,
            minX, maxY, maxZ, width, color6.red, color6.green, color6.blue, color6.alpha,
            maxX, maxY, maxZ, width, color7.red, color7.green, color7.blue, color7.alpha,
        )
    }

    private fun rainbow(offset: Long): Color {
        val hue = ((System.currentTimeMillis() + offset) % 10240L).toFloat() / 10240.0f
        return Color(Color.HSBtoRGB(hue, 1.0f, 1.0f))
    }
}
