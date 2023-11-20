package org.aresclient.ares.impl.instrument.module.modules.render

import net.meshmc.mesh.loader.Mod.Interface
import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Grouped
import org.aresclient.ares.api.setting.Grouped.Group
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.util.Color

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

    @Interface
    private lateinit var vsc: VSC
    interface VSC {
        fun render(delta: Float, buffers: Renderer.Buffers?, matrixStack: MatrixStack?, entities: Grouped<EntityTrait, Type>)
    }

    override fun onRenderWorld(delta: Float, buffers: Renderer.Buffers?, matrixStack: MatrixStack?) {
        vsc.render(delta, buffers, matrixStack, entities)
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
