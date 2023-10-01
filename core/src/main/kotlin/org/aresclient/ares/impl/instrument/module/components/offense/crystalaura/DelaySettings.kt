package org.aresclient.ares.impl.instrument.module.components.offense.crystalaura

import org.aresclient.ares.api.instrument.Component.Settings
import org.aresclient.ares.impl.instrument.global.Interaction
import org.aresclient.ares.impl.instrument.module.modules.offense.CrystalAura

object DelaySettings: Settings<CrystalAura>(CrystalAura, "Delays") {
    init {
        settings.setDescription("Settings which are either some form of delay, or modifies how fast the CA operates.")
    }

    val place = Interaction.standardDelay(settings, "Place", 5.0)
    val offhand_place = Interaction.standardDelay(settings, "Offhand Place", 5.0)
    val break_ = Interaction.standardDelay(settings, "Break", 5.0)
    val offhand_break = Interaction.standardDelay(settings, "Offhand Break", 5.0)

    val minimum_break_age = Interaction.tickOnlyDelay(settings, "Minimum Break Age", 2)
        .setDescription(
            "The minimum age, in ticks, the crystal must be before attempting to break it",
            "Recommendation for 2b2t: 4 ticks"
        )

    val break_on_spawn = settings
        .addBoolean("Break On Spawn", false)
        .setHidden { minimum_break_age.value > 0 }
        .setDescription("Attempt to break a crystal as soon as it spawns into the world")

    val conserve = settings
        .addBoolean("Conserve", true)
        .setDescription(
            "Attempt to conserve crystal inventory by automatically adjusting",
            "delays within the specified range situationally."
        )

    val maximum_place = Interaction.standardDelay(settings, "Maximum Place", 5.0)
        .setHidden { !conserve.value }
        .setDescription("The maximum place delay to intentionally wait while conserving.")

    val maximum_break = Interaction.standardDelay(settings, "Maximum Break", 5.0)
        .setHidden { !conserve.value }
        .setDescription("The maximum break delay to intentionally wait while conserving.")

    val maximum_break_age = Interaction.tickOnlyDelay(settings, "Maximum Break Age", 10)
        .setHidden { !conserve.value }
        .setDescription(
            "The maximum number of ticks to intentionally wait after a crystal",
            "has spawned before attempting to break it while conserving.",
            "FYI: damage is dealt at a maximum of once every 10 ticks."
        )

    val post_swap = Interaction.standardDelay(settings, "Post Swap", 0.0)
        .setDescription("The delay after swapping to crystals in the mainhand to wait before attempting to place")

    val offhand_post_swap = Interaction.standardDelay(settings, "Offhand Post Swap", 0.0)
        .setDescription("The delay after swapping to crystals in the offhand to wait before attempting to place")
}