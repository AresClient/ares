package org.aresclient.ares.impl.instrument.module.modules.player

import net.minecraft.util.math.Vec2f
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.instrument.global.Rotation
import org.aresclient.ares.impl.instrument.global.Rotator
import org.aresclient.ares.impl.util.MathUtil.duplicate
import org.aresclient.ares.impl.util.MathUtil.set
import org.aresclient.ares.impl.util.Timer

object AntiAFK: Module(Category.PLAYER, "AntiAFK", "Prevents player from being kicked due to being AFK"), Rotator {

    private val yaw_step = settings.addFloat("Yaw Step", 15F)
        .setMin(0F)
        .setMax(180F)

    // ════════════════════════════════════════════════════════════════════════ //

    override fun priority(): Int = 0
    override val yawStep: Float get() = yaw_step.value
    override val rotation: Vec2f = Vec2f.ZERO.duplicate()

    // ════════════════════════════════════════════════════════════════════════ //

    var a = 0f
    val timer = Timer()

    // ════════════════════════════════════════════════════════════════════════ //

    override fun onEnable() {
        Rotation.begin(this)
    }

    override fun onDisable() {
        Rotation.end(this)
    }

    override fun onTick() {
        if(timer.hasSecondsPassed(3)) {
            a += 90
            timer.reset()
        }
        rotation.set(a, 0f)
    }
}
