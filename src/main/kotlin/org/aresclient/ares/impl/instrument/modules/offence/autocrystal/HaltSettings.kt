package org.aresclient.ares.impl.instrument.modules.offence.autocrystal

import org.aresclient.ares.api.instruments.Component

object HaltSettings: Component.Settings<AutoCrystal>(AutoCrystal, "Halt") {
    init {
        settings.setDescription("Settings which pause or disable the module with certain conditions")
    }

    val pause_on_eat = settings
        .addBoolean("Pause on Eat", true)

    val pause_on_pot = settings
        .addBoolean("Pause on Pot", true)

    val pause_on_xp = settings
        .addBoolean("Pause on XP", true)

    val pause_on_mine = settings
        .addBoolean("Pause on Mine", true)

    val disable_if_dead = settings
        .addBoolean("Disable if Dead", true)
}