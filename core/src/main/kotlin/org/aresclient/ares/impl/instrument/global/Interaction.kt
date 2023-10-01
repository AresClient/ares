package org.aresclient.ares.impl.instrument.global

import org.aresclient.ares.api.instrument.global.Global
import org.aresclient.ares.api.setting.Setting
import kotlin.math.ceil

interface Placer: Prioritizer {
    fun placeDelay() = Interaction.delaySettingAsMillis(Interaction.block_place_delay)
    fun placeDelayTick() = Interaction.millisToTick(placeDelay())
    fun placePerInstant() = Interaction.place_per_instant.value
}

interface Breaker: Prioritizer {
    fun breakDelay() = Interaction.delaySettingAsMillis(Interaction.block_break_delay)
    fun breakDelayTick() = Interaction.millisToTick(breakDelay())
}

interface Crystaller: Prioritizer {
    fun crystalPlaceDelay() = Interaction.delaySettingAsMillis(Interaction.crystal_place_delay)
    fun crystalPlaceDelayTick() = Interaction.millisToTick(crystalPlaceDelay())
}

interface Attacker: Prioritizer {
    fun attackDelay() = Interaction.delaySettingAsMillis(Interaction.attack_delay)
    fun attackDelayTick() = Interaction.millisToTick(attackDelay())
}

object Interaction: Global("Interaction", "Handles player interactions with blocks and entities") {
    enum class RateType {
        TICK,
        MILLISECOND // TODO: Separate Multithreaded Millisecond tick
    }

    val percentageMeasure = "Measure: Percentage of a second"
    val percentageMeasureTick = "If Global/Interaction/Delay_Type is set to Tick this only functions in units of 5"
    val percentageMeasureArray = arrayOf(percentageMeasure, percentageMeasureTick)
    val tickMeasure = "Measure: Tick (One twentieth of a second)"
    val instantDefine = "One instant is one tick or millisecond, depending on whatever Global/Interaction/Delay_Type is set to"

    val delay_type = settings.addEnum("Delay Type", RateType.TICK)
        .setDescription(
            "The way delays are timed",
            "Tick - Minecraft's client tick which runs once every twentieth of a second (1/20)",
            "Millisecond - Use a separate thread which runs once every thousandth of a second (1/1000)"
        )

    // Use as a percentage with one decimal point so that it makes sense as both a tick and millisecond setting
    fun standardDelay(settings: Setting.Map<*>, name: String, default: Double): Setting.Number<Double> = settings
        .addDouble(name, default)
        .setMin(0.0)
        .setMax(100.0)
        .setPrecision(1)
        .setDescription(*percentageMeasureArray)

    fun tickOnlyDelay(settings: Setting.Map<*>, name: String, default: Int): Setting.Number<Int> = settings
        .addInteger(name, default)
        .setMin(0)
        .setMax(20)
        .setDescription(tickMeasure)

    val block_place_delay = standardDelay(settings, "Block Place Delay", 5.0)

    val place_per_instant = settings
        .addInteger("Blocks Placed Per Instant", 1)
        .setMin(1)
        .setMax(20)
        .setHidden { block_place_delay.value == 0.0 }
        .setDescription(*percentageMeasureArray)

    val block_break_delay = standardDelay(settings, "Block Break Delay", 5.0)
    val crystal_place_delay = standardDelay(settings, "Crystal Place Delay", 5.0)
    val attack_delay = standardDelay(settings, "Attack Delay", 62.5)

    fun delaySettingAsMillis(setting: Setting.Number<Double>) = (setting.value * 10).toInt()
    fun millisToTick(value: Int) = ceil(value.toDouble() / 50).toInt() // ceil because higher is safer with delays
}