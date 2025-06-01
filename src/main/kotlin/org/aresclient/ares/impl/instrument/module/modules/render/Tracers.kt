package org.aresclient.ares.impl.instrument.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.hud.HudDrawer
import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.instrument.module.modules.player.Freecam
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
            fun create(title: String, members: Collection<Any>, target: EntityUtil.Target, distance: Boolean = false, freecam: Boolean = false, enabled: Boolean = true): EntityGroup {
                return EntityGroup(HashSet(members)).also {
                    it.title.value = title
                    it.distance.value = distance
                    it.color.value = target.defaultColor
                    it.color.isRainbow = target.defaultRainbow
                    it.freecam.value = freecam
                    it.enabled.value = enabled
                }
            }
        }

        val distance: BooleanSetting = addBoolean("Distance", false)
        val color: ColorSetting = addColor("Color", Color.WHITE).setVisibility { !distance.value } as ColorSetting
        val freecam: BooleanSetting = addBoolean("Freecam Only", false)
    }

    private val entities = settings.addGrouped("Entities", arrayListOf(
        EntityGroup.create("Self", listOf(PlayerThreat.SELF), PlayerThreat.SELF, freecam = true),
        EntityGroup.create("Friends", listOf(PlayerThreat.FRIEND), PlayerThreat.FRIEND),
        EntityGroup.create("Players", listOf(PlayerThreat.HOSTILE), PlayerThreat.HOSTILE),
        EntityGroup.create("Monsters", EntityUtil.EntityTypes.monster, EntityUtil.TargetType.HOSTILE, enabled = false),
        EntityGroup.create("Animals", EntityUtil.EntityTypes.animal, EntityUtil.TargetType.PASSIVE, enabled = false),
        EntityGroup.create("Misc", EntityUtil.EntityTypes.miscellaneous.filter { it != EntityType.ITEM }, EntityUtil.TargetType.OTHER, enabled = false),
        EntityGroup.create("Items", listOf(EntityType.ITEM), EntityUtil.TargetType.ITEM, enabled = false)
    ), EntityUtil.EntityTypes.possibles, { EntityGroup() })

    private val entitiesCache = hashMapOf<Any, EntityGroup?>()
    private val mvp = Matrix4f()

    override fun onTick() {
        entitiesCache.clear()
    }

    private fun getEntityGroup(entity: Entity): EntityGroup? {
        val type = if(entity is PlayerEntity) entity.playerThreat else entity.type as Any
        return entitiesCache.getOrPut(type) { entities.find(type).getOrNull() }
    }

    override fun onRenderWorld(matrixStack: MatrixStack, delta: Float) {
        val center = Vector2f(MC.window.scaledWidth.toFloat(), MC.window.scaledHeight.toFloat()).div(2f)
        RenderSystem.getModelViewMatrix().mul(RenderSystem.getProjectionMatrix(), mvp)

        WORLD.entities?.forEach { entity ->
            val group = getEntityGroup(entity) ?: return@forEach
            if(!group.enabled.value || (group.freecam.value && !Freecam.isEnabled())) return@forEach

            val pos = entity.getLerpedRenderPos(delta)
            val color = if(group.distance.value) Color.fromDistance(SELF.distanceTo(entity)) else group.color.value

            tryDrawTracer(matrixStack, center, pos, pos.add(0.0, entity.height.toDouble(), 0.0), 1f, color)
        }
    }

    private fun Vec3d.toScreenPos(): Vector2f? {
        val pos = Vector4f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), 1f).mul(mvp)
        if(pos.w <= 0f) return null
        pos.div(pos.w)
        return Vector2f((pos.x + 1f) * MC.window.scaledWidth.toFloat() * 0.5f, MC.window.scaledHeight.toFloat() - (pos.y + 1f) * MC.window.scaledHeight.toFloat() * 0.5f)
    }

    private fun Entity.getLerpedRenderPos(delta: Float): Vec3d = getLerpedPos(delta).subtract(CAMERA.pos)

    private fun tryDrawTracer(matrixStack: MatrixStack, center: Vector2f, one: Vec3d, two: Vec3d, width: Float, color: Color) {
        drawTracer(matrixStack, center, one.toScreenPos() ?: return, two.toScreenPos() ?: return, width, color)
    }

    private fun drawTracer(matrixStack: MatrixStack, center: Vector2f, one: Vector2f, two: Vector2f, width: Float, color: Color) {
        HudDrawer.drawLine(matrixStack, center.x, center.y, one.x, one.y, color, width)
        HudDrawer.drawLine(matrixStack, one.x, one.y, two.x, two.y, color, width)
    }
}
