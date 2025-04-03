package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Grouped
import org.aresclient.ares.api.setting.Grouped.Group
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.instrument.global.Render

object ESP: Module(Category.RENDER, "ESP", "See outlines of players through walls") {
    class EntityTrait(settings: Setting.Map<*>?): Grouped.Trait(settings) {
        val color = settings?.addColor("Color", Color.RED)
    }

    enum class Type {
        PLAYER, OTHER
    }

    private val entities = settings.addGrouped("Entities", EntityTrait::class.java, Type::class.java,
        { map -> Group("Player", EntityTrait(map).also { it.color?.value = Color.RED }, mutableListOf(Type.PLAYER))},
        { map -> Group("Other", EntityTrait(map).also { it.color?.value = Color.BLUE }, mutableListOf(Type.OTHER))}
    )

    override fun onRenderWorld(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) {
        MC.world?.entities?.forEach { entity ->
            val type = if(entity.type == EntityType.PLAYER) Type.PLAYER else Type.OTHER
            entities.trait(type)?.color?.value?.let {
                Render.Lines.box(entity.getInterpolatedBoundingBox(delta), it, 2f)
            }
        }
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z
        return Box(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }

    /*private val entities = settings.grouped("Entities", arrayListOf(
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
