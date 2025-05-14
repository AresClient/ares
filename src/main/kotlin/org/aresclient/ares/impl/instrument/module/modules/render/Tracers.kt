package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.EntityUtil
import org.aresclient.ares.impl.util.EntityUtil.PlayerThreat
import org.aresclient.ares.impl.util.EntityUtil.playerThreat
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import kotlin.jvm.optionals.getOrNull

object Tracers: Module(Category.RENDER, "Tracers", "Render lines showing entities in render distance") {
    private class EntityGroup(members: Set<Any> = emptySet()): Group<Any>(members) {
        companion object {
            fun create(title: String, members: Collection<Any>, distance: Boolean = false, color: Color = Color.WHITE, rainbow: Boolean = false, enabled: Boolean = true): EntityGroup {
                return EntityGroup(HashSet(members)).also {
                    it.title.value = title
                    it.distance.value = distance
                    it.color.value = color
                    it.color.isRainbow = rainbow
                    it.enabled.value = enabled
                }
            }


            fun create(title: String, members: Collection<Any>, target: EntityUtil.Target, enabled: Boolean = true): EntityGroup {
                return create(title, members, color = target.defaultColor, rainbow = target.defaultRainbow, enabled = enabled)
            }
        }

        val distance: BooleanSetting = addBoolean("Distance", false)
        val color: ColorSetting = addColor("Color", Color.WHITE).setVisibility { !distance.value } as ColorSetting
    }

    private val entities = settings.addGrouped("Entities", arrayListOf(
        EntityGroup.create("Self", listOf(PlayerThreat.SELF), PlayerThreat.SELF),
        EntityGroup.create("Friends", listOf(PlayerThreat.FRIEND), PlayerThreat.FRIEND),
        EntityGroup.create("Players", listOf(PlayerThreat.HOSTILE), PlayerThreat.HOSTILE),
        EntityGroup.create("Monsters", EntityUtil.EntityTypes.monster, EntityUtil.TargetType.HOSTILE, enabled = false),
        EntityGroup.create("Animals", EntityUtil.EntityTypes.animal, EntityUtil.TargetType.PASSIVE, enabled = false),
        EntityGroup.create("Misc", EntityUtil.EntityTypes.miscellaneous.filter { it != EntityType.ITEM }, EntityUtil.TargetType.OTHER, enabled = false),
        EntityGroup.create("Items", listOf(EntityType.ITEM), EntityUtil.TargetType.ITEM, enabled = false)
    ), EntityUtil.EntityTypes.possibles, { EntityGroup() })

    private val entitiesCache = hashMapOf<Any, EntityGroup?>()

    override fun onTick() {
        entitiesCache.clear()
    }

    private fun getEntityGroup(entity: Entity): EntityGroup? {
        val type = if(entity is PlayerEntity) entity.playerThreat else entity.type as Any
        return entitiesCache.getOrPut(type) { entities.find(type).getOrNull() }
    }

    override fun onRenderWorld2d(delta: Float, renderer: Renderer.State, projection: Matrix4f) {
        val center = Vector2f(MC.window.framebufferWidth.toFloat(), MC.window.framebufferHeight.toFloat()).div(2f)

        WORLD.entities?.forEach { entity ->
            val group = getEntityGroup(entity) ?: return@forEach
            if(!group.enabled.value) return@forEach

            val pos = entity.getLerpedRenderPos(delta)
            val color = if(group.distance.value) Color.fromDistance(SELF.distanceTo(entity)) else group.color.value

            renderer.tryDrawTracer(projection, center, pos, pos.add(0.0, entity.height.toDouble(), 0.0), 1f, color)
        }
    }

    private fun Vec3d.toScreenPos(projection: Matrix4f): Vector2f? {
        val pos = Vector4f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), 1f).mul(projection)
        if(pos.w <= 0f) return null
        pos.div(pos.w)
        return Vector2f((pos.x + 1f) * MC.window.framebufferWidth.toFloat() * 0.5f, MC.window.framebufferHeight.toFloat() - (pos.y + 1f) * MC.window.framebufferHeight.toFloat() * 0.5f)
    }

    private fun Entity.getLerpedRenderPos(delta: Float): Vec3d = getLerpedPos(delta).subtract(CAMERA.pos)

    private fun Renderer.State.tryDrawTracer(projection: Matrix4f, center: Vector2f, one: Vec3d, two: Vec3d, width: Float, color: Color) {
        drawTracer(center, one.toScreenPos(projection) ?: return, two.toScreenPos(projection) ?: return, width, color)
    }

    private fun Renderer.State.drawTracer(center: Vector2f, one: Vector2f, two: Vector2f, width: Float, color: Color) {
        buffers.lines.indicesOffset(0, 1, 1, 2)
        buffers.lines.vertices(
            center.x, center.y, 1f, width, color.red, color.green, color.blue, color.alpha,
            one.x, one.y, 1f, width, color.red, color.green, color.blue, color.alpha,
            two.x, two.y, 1f, width, color.red, color.green, color.blue, color.alpha,
        )
    }
}
