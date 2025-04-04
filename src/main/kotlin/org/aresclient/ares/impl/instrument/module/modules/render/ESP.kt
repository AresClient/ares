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
           if(entity != MC.player) {
               val type = if(entity.type == EntityType.PLAYER) Type.PLAYER else Type.OTHER
               entities.trait(type)?.color?.value?.let {
                   Render.Lines.box(entity.getInterpolatedBoundingBox(delta), it, 2f)
               }
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
}
