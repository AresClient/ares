package org.aresclient.ares.impl.module.player

import org.aresclient.ares.api.module.Category
import org.aresclient.ares.api.module.Module
import org.aresclient.ares.impl.global.Rotation
import org.aresclient.ares.impl.global.Rotator
import org.aresclient.ares.impl.util.Timer

/**
 * TODO: This will only fully function when the player is already moving until the rotations are swapped to
 *      replacing the sendMovementPackets method rather than replacing already sent packets
 */
object AntiAFK: Module(Category.PLAYER, "AntiAFK", "Prevents player from being kicked due to being AFK"), Rotator {
    private val yaw_step = settings.addFloat("Yaw Step", 15F)

    override fun priority(): Int = 0
    override fun yawStep(): Float = yaw_step.value

    var a = 0f
    val timer = Timer()

    override fun onTick() {
        if(timer.hasSecondsPassed(3)) {
            a += 90
            timer.reset()
        }
        Rotation.setRotation(a, 0f, this)
    }
}