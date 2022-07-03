package org.aresclient.ares.module.render

import net.meshmc.mesh.api.entity.Entity
import net.meshmc.mesh.api.entity.EntityType
import net.meshmc.mesh.api.math.Box
import net.meshmc.mesh.util.math.MathHelper
import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.global.RenderGlobal
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module

object ESP: Module("ESP", "See outlines of players through walls", Category.RENDER, enabled = true) {
    private val color = settings.color("Color", Color.RED)

    override fun onRenderWorld(event: RenderGlobal.Event) {
        val color0 = rainbow(1280L)
        val color1 = rainbow(2560L)
        val color2 = rainbow(3840L)
        val color3 = rainbow(5120L)
        val color4 = rainbow(6400L)
        val color5 = rainbow(7680L)
        val color6 = rainbow(8960L)
        val color7 = rainbow(10240L)

        MC.world.loadedEntities.forEach {
            if(it.entityType == EntityType.PLAYER && it != MC.player)
                RenderGlobal.Lines.box(it.getInterpolatedBoundingBox(event.delta), color0, color1, color2, color3, color4, color5, color6, color7, 2f)
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

    private fun rainbow(offset: Long): Color {
        val hue = ((System.currentTimeMillis() + offset) % 10240L).toFloat() / 10240.0f
        return Color(Color.HSBtoRGB(hue, 1.0f, 1.0f))
    }
}
