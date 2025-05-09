package org.aresclient.ares.impl.instrument.module.modules.movement

import org.aresclient.ares.api.instruments.Module

object SafeWalk: Module(Category.MOVEMENT, "SafeWalk", "Keeps you from walking off ledges") {
    private val tolerance = settings.addDouble("Tolerance", 0.0).setMin(0.0).setMax(0.5)

    fun getTolerance(): Double = tolerance.value

    // see MixinPlayerEntity
}
