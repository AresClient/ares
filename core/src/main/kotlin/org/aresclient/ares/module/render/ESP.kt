package org.aresclient.ares.module.render

import org.aresclient.ares.Group
import org.aresclient.ares.GroupTrait
import org.aresclient.ares.SColor
import org.aresclient.ares.SColor.Companion.toSColor
import org.aresclient.ares.Settings
import org.aresclient.ares.api.Entity
import org.aresclient.ares.api.EntityType
import org.aresclient.ares.api.math.Box
import org.aresclient.ares.global.RenderGlobal
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.Color
import org.aresclient.ares.utils.MathHelper

object ESP: Module("ESP", "See outlines of players through walls", Category.RENDER) {
    private class EntityTrait(defaultColor: SColor, settings: Settings = Settings.new()): GroupTrait(settings) {
        val color = settings.color("Color", defaultColor)
    }

    private val entities = settings.grouped("liEntities", arrayListOf(
        Group("Player", EntityTrait(SColor.rainbow()), arrayListOf(EntityType.PLAYER))
    ), EntityType.values().asList()) { EntityTrait(Color.RED.toSColor(), it) }

    override fun onRenderWorld(event: RenderGlobal.Event) {
        MC.getLoadedEntities().forEach { entity ->
            if(entity.isSameAs(MC.getPlayer())) entities.trait(entity.entityType)?.let { trait ->
                val color = trait.color.value.getColors(8)
                RenderGlobal.Lines.box(entity.getInterpolatedBoundingBox(event.delta), color[0], color[1], color[2], color[3], color[4], color[5], color[6], color[7], 2f)
            }
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
}
