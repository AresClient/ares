package org.aresclient.ares.impl.iface.module.movement

import net.meshmc.mesh.loader.Mod.Interface
import net.minecraft.client.MinecraftClient
import org.aresclient.ares.impl.instrument.module.modules.movement.Strafe
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Interface
class StrafeVSC: Strafe.VSC {
    override fun strafe(strafe: Strafe) {
        val mc = MinecraftClient.getInstance()

        if(mc.player!!.input.movementForward != 0f || mc.player!!.input.movementSideways != 0f) {
            if(strafe.sprintBool.value) mc.player!!.setSprinting(true)
            if(mc.player!!.isOnGround) {
                if(strafe.lowHop.value) mc.player!!.addVelocity(0.0, strafe.height.value, 0.0)
                return
            }

            val speed =
                if(!strafe.speedBool.value)
                    sqrt(mc.player!!.velocity.x * mc.player!!.velocity.x + mc.player!!.velocity.z * mc.player!!.velocity.z)
                else
                    strafe.speedVal.value

            var yaw: Float = mc.player!!.yaw
            var forward = 1f
            if(mc.player!!.forwardSpeed < 0) {
                yaw += 180f
                forward = -0.5f
            } else if(mc.player!!.forwardSpeed > 0) forward = 0.5f
            if(mc.player!!.sidewaysSpeed > 0) yaw -= 90 * forward
            if(mc.player!!.sidewaysSpeed < 0) yaw += 90 * forward
            yaw = Math.toRadians(yaw.toDouble()).toFloat()

            mc.player!!.setVelocity(-sin(yaw.toDouble()) * speed, mc.player!!.velocity.y, cos(yaw.toDouble()) * speed)
        }
    }
}