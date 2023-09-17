package org.aresclient.ares.impl.instrument.module.components.offense.crystalaura

import org.aresclient.ares.api.instrument.Component.Settings
import org.aresclient.ares.impl.instrument.module.modules.offense.CrystalAura

object CalculationSettings: Settings<CrystalAura>(CrystalAura, "Calculation") {
    init {
        settings.appendLines("Settings which are related to calculation and deciding where the CrystalAura should place")
    }

    val damage_calculation = settings
        .addEnum("Damage Calculation", DamageCalculation.DAMAGE)

    enum class DamageCalculation {
        DAMAGE,
        DISTANCE
    }

    val predict_position = settings
        .addBoolean("Predict Position", true)
        .appendLines("Attempt to predict the position of the player when placing")
        .appendLines("based on the velocity of the player.")

    // TODO: settings related to movement prediction

    val predict_surround_opening = settings
        .addBoolean("Predict Surround Opening", true)
        .appendLines("Attempt to predict when a block in surround will be opened")
        .appendLines("by another player based on the current break progress in")
        .appendLines("conjunction with the player's latency.")

    val pre_occlude_surround = settings
        .addBoolean("Pre Occlude Surround", true)
        .setHidden { !predict_surround_opening.value }
        .appendLines("When predicting that the surround openings, attempt to")
        .appendLines("occlude the surround block early by placing a crystal next")
        .appendLines("to it in a position that would occlude the block from being")
        .appendLines("immediately replaced.")

    val occlude_while_mining = settings
        .addBoolean("Occlude While Mining", true)
        .setHidden { !pre_occlude_surround.value }
        .appendLines("Allow CrystalAura to attempt to place crystals while packet")
        .appendLines("mining blocks to pre-occlude the surround.")

    val surround_short_circuit = settings
        .addBoolean("Surround Short Circuit", true)
        .appendLines("Tries to place a crystal as soon as the surround is opened")
        .appendLines("to try and break the surround.")

    val use_predict_for_short_circuit = settings
        .addBoolean("Use Predict For Short Circuit", false)
        .setHidden { !surround_short_circuit.value }
        .appendLines("Uses the surround opening prediction to try and predict timing for")
        .appendLines("short circuiting to break the surround.")
}