package org.aresclient.ares.impl.instrument.modules.render.esp

import net.minecraft.client.render.Frustum
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.drawer.WorldDrawer
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.EnumSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.EntityUtil
import org.aresclient.ares.impl.util.EntityUtil.PlayerThreat
import org.aresclient.ares.impl.util.EntityUtil.playerThreat
import org.aresclient.ares.mixin.accessors.AccessWorldRenderer
import kotlin.jvm.optionals.getOrNull

// TODO: FIX DEPTH ON OUTLINE ESP
object ESP: Module(Category.RENDER, "ESP", "See outlines of entities through walls") {
    enum class Mode { OUTLINE, BOX, CHAMLIKE }

    open class RenderMode: Component<ESP>(this) {
        open fun draw(entity: Entity, group: EntityGroup, drawer: WorldDrawer, delta: Float) {
        }
    }

    init {
        OutlineESP
        BoxESP
        ChamlikeESP
    }

    class EntityGroup(members: Set<Any> = emptySet()): Group<Any>(members) {
        companion object {
            fun create(title: String, members: Collection<Any>, mode: Mode, target: EntityUtil.Target, enabled: Boolean = false): EntityGroup {
                return EntityGroup(HashSet(members)).also {
                    it.title.value = title
                    it.mode.value = mode
                    it.lineColor.value = target.defaultColor
                    it.lineColor.isRainbow = target.defaultRainbow
                    it.fillColor.value = target.defaultColor.deriveAlpha(0.2f)
                    it.fillColor.isRainbow = target.defaultRainbow
                    it.enabled.value = enabled
                }
            }
        }

        val mode: EnumSetting<Mode> = addEnum("Mode", Mode.OUTLINE)
        val lineColor: ColorSetting = addColor("Line Color", Color.WHITE)
        val fillColor: ColorSetting = addColor("Fill Color", Color.WHITE.deriveAlpha(0.2f)).setVisibility { mode.value != Mode.OUTLINE } as ColorSetting
    }

    private val entities = settings.addGrouped("Entities", arrayListOf(
        EntityGroup.create("Friends", listOf(PlayerThreat.FRIEND), Mode.OUTLINE, PlayerThreat.FRIEND, enabled = true),
        EntityGroup.create("Players", listOf(PlayerThreat.HOSTILE), Mode.OUTLINE, PlayerThreat.HOSTILE, enabled = true),
        EntityGroup.create(
            "Crystals",
            listOf(EntityType.END_CRYSTAL),
            Mode.CHAMLIKE,
            EntityUtil.TargetType.END_CRYSTAL,
            enabled = true
        ),
        EntityGroup.create("Monsters", EntityUtil.EntityTypes.monster, Mode.OUTLINE, EntityUtil.TargetType.HOSTILE),
        EntityGroup.create("Animals", EntityUtil.EntityTypes.animal, Mode.OUTLINE, EntityUtil.TargetType.PASSIVE),
        EntityGroup.create("Items", listOf(EntityType.ITEM), Mode.OUTLINE, EntityUtil.TargetType.ITEM, enabled = true),
        EntityGroup.create(
            "Misc",
            EntityUtil.EntityTypes.miscellaneous.filter { it != EntityType.END_CRYSTAL && it != EntityType.ITEM },
            Mode.OUTLINE,
            EntityUtil.TargetType.OTHER
        )
    ), EntityUtil.EntityTypes.possibles, { EntityGroup() })

    private val entitiesCache = hashMapOf<Any, EntityGroup?>()

    override fun onTick() {
        entitiesCache.clear()
    }

    override fun onRenderWorld(drawer: WorldDrawer, delta: Float) {
        if(entities.none { it.enabled.value && it.mode.value != Mode.OUTLINE }) return

        ChamlikeESP.begin()

        val frustum = Frustum(MC.worldRenderer.capturedFrustum ?: (MC.worldRenderer as AccessWorldRenderer).frustum)
        WORLD.entities?.forEach { entity ->
            if(entity == SELF) return@forEach

            val group = getEntityGroup(entity) ?: return@forEach
            if(!group.enabled.value || group.mode.value == Mode.OUTLINE || entity.shouldCull(frustum)) return@forEach

            if(group.mode.value == Mode.CHAMLIKE) ChamlikeESP.draw(entity, group, drawer, delta)
            else if(group.mode.value == Mode.BOX) BoxESP.draw(entity, group, drawer, delta)
        }

        ChamlikeESP.end()
    }

    fun getEntityGroup(entity: Entity): EntityGroup? {
        val type = if(entity is PlayerEntity) entity.playerThreat else entity.type as Any
        return entitiesCache.getOrPut(type) { entities.find(type).getOrNull() }
    }

    private fun <T: Entity> T.shouldCull(frustum: Frustum): Boolean {
        if(this is DisplayEntity && !shouldRender()) return true
        return when(this) {
            is EnderDragonEntity, is FishingBobberEntity, is LightningEntity -> false
            else -> !frustum.isVisible(boundingBox.expand(0.5))
        }
    }
}
