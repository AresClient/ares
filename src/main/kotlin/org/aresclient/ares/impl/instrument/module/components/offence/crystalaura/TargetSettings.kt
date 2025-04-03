package org.aresclient.ares.impl.instrument.module.components.offence.crystalaura

import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.impl.instrument.module.modules.offence.CrystalAura

object TargetSettings: Component.Settings<CrystalAura>(CrystalAura, "Target") {
    val players = settings.addBoolean("Players", true)

    val friends = settings
        .addBoolean("Friends", false)
        .setVisibility { players.value }

    val prioritize_player = settings
        .addString("Prioritize Player", "Fit")
        .setVisibility { players.value }

    val mobs = settings.addBoolean("Mobs", false)
    val animals = settings.addBoolean("Animals", false)
    val pets = settings.addBoolean("Pets", false)
    val boss = settings.addBoolean("Boss", true)

    val target_priority = settings
    .addEnum("Target Priority", TargetPriority.MOST_DAMAGE)

    enum class TargetPriority {
        CLOSEST,
        MOST_DAMAGE,
        LOOKING_AT,
        LOWEST_HEALTH,
        LOWEST_ARMOR,
        MOST_TARGETS
    }
}