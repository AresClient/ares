package org.aresclient.ares.impl.instrument.module.modules.render

import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module

object ESP: Module(Category.RENDER, "ESP", "See outlines of players through walls") {
    /*private class EntityTrait(defaultColor: Color, settings: Settings = Settings.new()): GroupTrait(settings) {
        val color = settings.addColor("Color", defaultColor)
    }

    private val entities = settings.grouped("Entities", arrayListOf(
        Group("Player", EntityTrait(SColor.rainbow()), arrayListOf(EntityType.PLAYER))
    ), EntityType.values().asList()) { EntityTrait(Color.RED.toSColor(), it) }*/

    /*override fun onRenderWorld(delta: Float, buffers: Renderer.Buffers?, matrixStack: MatrixStack?) {
        Mesh.getMesh().minecraft.world.loadedEntities.forEach { entity ->
            // TODO: ignore player
            //entities.trait(entity.entityType)?.let { trait ->
                //val color = trait.color.value.getColors(8)
                val color = Theme.current().primary.getValues(8)
                RenderGlobal.Lines.box(entity.getInterpolatedBoundingBox(delta), color[0], color[1], color[2], color[3], color[4], color[5], color[6], color[7], 2f)
            //}
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

     */
}
