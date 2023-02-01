package org.aresclient.ares.module.player

import org.aresclient.ares.global.RotationGlobal
import org.aresclient.ares.global.Rotator
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.utils.Timer

/**
 * TODO: This will only fully function when the player is already moving until the rotations are swapped to
 *      replacing the sendMovementPackets method rather than replacing already sent packets
 */
object AntiAFK: Module("AntiAFK", "Prevents player from being kicked due to being AFK", Category.PLAYER), Rotator {
    val yaw_step = settings.float("Yaw Step", 15F)

    override fun getPriority(): Int = 0
    override fun getYawStep(): Float = yaw_step.value

    var a = 0f
    val timer = Timer()

    override fun onTick() {
        if(timer.hasSecondsPassed(3)) {
            a += 90
            timer.reset()
        }
        RotationGlobal.setRotation(a, 0f, this)
    }
}