package org.aresclient.ares.impl.instrument.module.components.offence.autocrystal

import net.minecraft.entity.EntityType
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.impl.instrument.module.modules.offence.AutoCrystal
import org.aresclient.ares.impl.util.EntityUtil.EntityTypes
import org.aresclient.ares.impl.util.EntityUtil.PlayerThreat

object TargetSettings: Component.Settings<AutoCrystal>(AutoCrystal, "Target") {
    class EntityGroup(members: Set<Any> = emptySet()): Group<Any>(members) {
        companion object {
            fun create(title: String, members: Set<Any>, enabled: Boolean = false): EntityGroup {
                return EntityGroup(members).also {
                    it.title.value = title
                    it.enabled.value = enabled
                }
            }
        }
    }

    val entities = settings.addGrouped("Entities", arrayListOf(
        EntityGroup.create("Players", setOf(PlayerThreat.HOSTILE, PlayerThreat.TEAM), enabled = true),
        EntityGroup.create("Friends", setOf(PlayerThreat.FRIEND)),
        EntityGroup.create("Monsters", EntityTypes.monster),
        EntityGroup.create("Animals", EntityTypes.animal),
        EntityGroup.create("Items", setOf(EntityType.ITEM)),
        EntityGroup.create("Misc", EntityTypes.miscellaneous.filter { it != EntityType.END_CRYSTAL && it != EntityType.ITEM }.toSet())
    ), EntityTypes.possibles, { EntityGroup() })

    enum class TargetPriority {
        CLOSEST,
        MOST_DAMAGE,
        LOOKING_AT,
        LOWEST_HEALTH,
        LOWEST_ARMOR,
        MOST_TARGETS
    }

    val target_priority = settings.addEnum("Target Priority", TargetPriority.MOST_DAMAGE)
}