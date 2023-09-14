package org.aresclient.ares.impl.instrument.global

import org.aresclient.ares.api.instrument.global.Global
import kotlin.math.round

interface Placer: Prioritizer {
    fun placeRate() = Interaction.placeRate(Interaction.placeRate.value)
    fun placeDelay() = 1000 - placeRate()
    fun placeDelayTick() = 20 - placeDelay() / 50
    fun placePerInstant() = Interaction.placePerInstant.value
}

interface Breaker: Prioritizer {
    fun breakRate() = Interaction.breakRate(Interaction.breakRate.value)
    fun breakDelay() = 1000 - breakRate()
    fun breakDelayTick() = 20 - breakRate() / 50
}

interface Crystaller: Prioritizer {
    fun crystalRate() = Interaction.crystalRate(Interaction.crystalRate.value)
    fun crystalDelay() = 1000 - crystalRate()
    fun crystalDelayTick() = 20 - crystalRate() / 50
}

interface Attacker: Prioritizer {
    fun attackRate() = Interaction.attackRate(Interaction.attackRate.value)
    fun attackDelay() = 1000 - attackRate()
    fun attackDelayTick() = 20 - attackRate() / 50
}

object Interaction: Global("Interaction", "Handles player interactions with blocks and entities") {
    enum class RateType {
        TICK,
        MILLISECOND // TODO: Separate Multithreaded Millisecond tick
    }

    val rateType = settings.addEnum("Rate Type", RateType.TICK,
        "The way delays are timed",
        "Tick - Minecraft's client tick which runs once every twentieth of a second (1/20)",
        "Millisecond - Use a separate thread which runs once every thousandth of a second (1/1000)"
    )

    // Use as a percentage with one decimal point so that it makes sense as both a tick and millisecond setting
    val placeRate = settings.addDouble("Block Place Rate %", 100.0).setMin(0.0).setMax(100.0) // TODO: Visually clamp setting to 1 decimal point
    val placePerInstant = settings.addInteger("Blocks Placed Per Instant", 1).setMin(1).setMax(20) // TODO: Only Visible when Place Rate is 100
    val breakRate = settings.addDouble("Block Break Rate %", 100.0).setMin(0.0).setMax(100.0)
    val crystalRate = settings.addDouble("Crystal Rate %", 100.0).setMin(0.0).setMax(100.0)
    val attackRate = settings.addDouble("Attack Rate %", 100.0).setMin(0.0).setMax(100.0)

    fun placeRate(rate: Double) = (round(rate * 10)).toInt()
    fun breakRate(rate: Double) = (round(rate * 10)).toInt()
    fun crystalRate(rate: Double) = (round(rate * 10)).toInt()
    fun attackRate(rate: Double) = (round(rate * 10)).toInt()
}