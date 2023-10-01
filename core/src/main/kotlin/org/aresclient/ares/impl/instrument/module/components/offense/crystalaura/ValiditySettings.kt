package org.aresclient.ares.impl.instrument.module.components.offense.crystalaura

import org.aresclient.ares.api.instrument.Component.Settings
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.instrument.module.modules.offense.CrystalAura

object ValiditySettings: Settings<CrystalAura>(CrystalAura, "Validity") {
    fun damageSetting(name: String, default: Float): Setting.Number<Float> = settings
        .addFloat(name, default)
        .setMin(0F)
        .setMax(15F)

    val place_minimum_damage_dealt = damageSetting("Place Minimum Damage Dealt", 7.5F)
    val break_minimum_damage_dealt = damageSetting("Break Minimum Damage Dealt", 7.5F)

    val place_maximum_self_inflicted = damageSetting("Place Maximum Self Inflicted", 1F)
    val break_maximum_self_inflicted = damageSetting("Break Maximum Self Inflicted", 1F)

    val prevent_suicide = settings
        .addBoolean("Prevent Suicide", true)
        .setDescription(
            "Prevents you from killing yourself if the allowed self",
            "inflicted damage is higher than your remaining health."
        )

    val place_maximum_friendly_fire = damageSetting("Place Maximum Friendly Fire", 1F)
    val break_maximum_friendly_fire = damageSetting("Break Maximum Friendly Fire", 1F)

    val prevent_fratricide = settings
        .addBoolean("Prevent Fratricide", true)
        .setDescription(
            "Prevents you from killing your friend if the allowed",
            "friendly fire damage is higher than his remaining health."
        )

    fun ratio(name: String, default: Int): Setting.Number<Int> = settings
        .addInteger(name, default)
        .setMin(1)
        .setMax(15)
        .setDescription(
            "The minimum ratio of self inflicted or friendly fire to damage",
            "dealt in a ratio of 1 to the value of this setting."
        )

    val minimum_place_ratio = ratio("Minimum Place Ratio", 10)
    val minimum_break_ratio = ratio("Minimum Break Ratio", 10)

    val raycast = settings
        .addBoolean("Raycast", true)

    val strict_sides = settings
        .addBoolean("Strict Sides", true)
        .setDescription(
            "Only place against block sides which are facing towards you",
            "and are not against another block.",
            "If CrystalAura/Validity/Place_Crystal_At is set to Strict_Side",
            "it will target the center of the strict blockface."
        )

    val one_point_twelve_placements = settings
        .addBoolean("1.12 Placements", false)
        .setDescription(
            "Only attempt to place a crystal if there are two blocks of",
            "air above the block, instead of one."
        )

    val place_crystal_at = settings
        .addEnum("Place Crystal At", InteractionPoint.CLOSEST_POINT)
        .addRestriction(InteractionPoint.STRICT_SIDE, { !strict_sides.value }, "Cannot set Place_Crystal_At to STRICT_SIDE when Strict_Sides is disabled.")
        .setDescription(
            "The point of the block at which to interact with when placing a crystal",
            "Closest Point - Interact with the block at the closest point relative to the player's eyelevel",
            "Strict Sides - Only to be used with CrystalAura/Validity/Strict_Sides enabled - see Strict Sides for info",
            "Center - Interact with the block at the center of the block"
        )

    enum class InteractionPoint {
        CLOSEST_POINT,
        STRICT_SIDE,
        CENTER
    }

    val measure_range_to = settings
        .addEnum("Measure Range To", MeasurePoint.CLOSEST_POINT)
        .setDescription(
            "The point of the block or entity box from which to",
            "measure the distance to the player's eyelevel."
        )

    enum class MeasurePoint {
        CLOSEST_POINT,
        CENTER
    }

    val use_game_range = settings
        .addBoolean("Use Game Range", true)
        .setDescription("Use the default range provided in the game's interaction manager")
}