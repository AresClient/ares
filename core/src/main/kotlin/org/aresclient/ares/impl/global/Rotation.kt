package org.aresclient.ares.impl.global

import org.aresclient.ares.api.global.Global
import org.aresclient.ares.api.minecraft.math.Vec2f
import org.aresclient.ares.impl.util.Timer

/**
 * TODO: I broke this, needs to be fixed -Tigermouthbear
 * TODO: Test whether interaction packets have to be sent right after the final step packet,
 *      at the end of the tick the packet's sent, or during the beginning of the next tick on strict;
 *      if it has to be the next tick, how soon after the interaction can the rotation be drastically changed
 */
interface Rotator: Prioritizer {
    fun yawStep(): Float = Rotation.yaw_step.value
    fun pitchStep(): Float = Rotation.pitch_step.value
}

object Rotation: Global("Rotation", "Handles player rotations so that the server thinks the player is facing a certain direction") {
    val reset_delay = settings.addLong("Reset Delay", 10).setMin(0).setMax(100)
    val yaw_step = settings.addFloat("Yaw Step", 180F)
    val pitch_step = settings.addFloat("Pitch Step", 180F)

    private var rotation: Vec2f? = null

    private var key: Rotator? = null
    private var released = true
    private var resetTimer: Timer = Timer()

    fun getRotation(): Vec2f? {
        return rotation?.duplicate()
    }

    fun setRotation(yaw: Float, pitch: Float, key: Rotator): Boolean {
        return setRotation(yaw, pitch, key, false)
    }

    fun setRotation(yaw: Float, pitch: Float, key: Rotator, instant: Boolean): Boolean {
        return setRotation(Vec2f.create(yaw, pitch), key, instant)
    }

    fun setRotation(rotation: Vec2f, key: Rotator): Boolean {
        return setRotation(rotation, key, false)
    }

    fun setRotation(rotation: Vec2f, key: Rotator, instant: Boolean): Boolean {
        if(Rotation.rotation == null || released || Priority.keyMatches(key) || Priority.hasPriority(key)) {
            // Rotate instantly if specified, but only if the rotation does not match (no need to spam)
            //if(instant && this.rotation != rotation) PlayerMoveC2SPacket.Rotation.create(rotation, MC.getPlayer().isOnGround) //TODO: send packet (once possible in mesh)

            Rotation.rotation = rotation
            Rotation.key = key
            released = false
            resetTimer.reset()
            steppingComplete = false

            return true
        }

        return false
    }

    private fun Vec2f.duplicate(): Vec2f {
        return Vec2f.create(x, y)
    }

    private fun Vec2f.set(rotation: Vec2f) {
        this.x = rotation.x
        this.y = rotation.y
    }

    fun normalizeAngle(angle: Float): Float {
        var a = angle % 360
        if(a >= 180) a -= 360
        if(a < -180) a += 360
        return a
    }

    private fun normalizeRotation(rotation: Vec2f): Vec2f {
        return Vec2f.create(normalizeAngle(rotation.x), normalizeAngle(rotation.y))
    }

    private fun getChange(angle1: Float, angle2: Float): Float {
        var a = angle1 - angle2
        if(a > 180) a -= 360
        else if(a < -180) a += 360
        return -a
    }

    private val lastRotation = Vec2f.create(0F, 0F)
    private val tempRotation = Vec2f.create(0F, 0F)
    private var steppingComplete = true

    fun isCompletedStepping(): Boolean {
        return steppingComplete
    }

    //TODO: switch to replacing EntityClientPlayerEvent.sendMovementPackets so that alternative packets can be sent without being modified?
    /*@field:EventHandler
    private val onMovementPacketSent = EventListener<PacketEvent.Sent> { event ->
        if(event.packet is PlayerMoveC2SPacket.Rotation) {
            val packet: PlayerMoveC2SPacket.Rotation = event.packet
            when(event.era) {
                PacketEvent.Era.BEFORE -> {
                    if(rotation == null) {
                        resetTimer.reset()
                        return@EventListener
                    }

                    //TODO: Global settings (separate panel?)
                    if(resetTimer.hasTicksPassed(reset_delay.value) && steppingComplete) {
                        rotation = null
                        key = null
                        released = true
                        resetTimer.reset()
                        return@EventListener
                    }

                    val lastRotNorm: Vec2f = normalizeRotation(lastRotation)
                    val rotNorm: Vec2f = normalizeRotation(rotation!!)

                    val yawStep = min(key!!.getYawStep(), yaw_step.value)
                    val pitchStep = min(key!!.getPitchStep(), pitch_step.value)

                    if(lastRotNorm.x != rotNorm.x && (yawStep != 180F || pitchStep != 180F)) {
                        val xChange = getChange(lastRotNorm.x, rotNorm.x)
                        val yChange = getChange(lastRotNorm.y, rotNorm.y)
                        tempRotation.x =
                            if(xChange > yawStep) lastRotation.x +yawStep
                            else if(xChange < -yawStep) lastRotation.x -yawStep
                            else rotation!!.x
                        tempRotation.y =
                            if(yChange > pitchStep) lastRotation.y +pitchStep
                            else if(yChange < -pitchStep) lastRotation.y -pitchStep
                            else rotation!!.y

                        packet.rotation = tempRotation
                        if(tempRotation.x == rotation!!.x && tempRotation.y == rotation!!.y) steppingComplete = true

                        MC.getPlayer().setRenderHeadYaw(tempRotation.x)
                        MC.getPlayer().setRenderBodyYaw(tempRotation.x)
                    } else {
                        packet.rotation = rotation
                        steppingComplete = true

                        MC.getPlayer().setRenderHeadYaw(rotation!!.x)
                        MC.getPlayer().setRenderBodyYaw(rotation!!.x)
                    }
                }
                PacketEvent.Era.AFTER -> {
                    lastRotation.set(packet.rotation)
                    rotation?.let {
                        if(lastRotation.x == it.x && lastRotation.y == it.y) steppingComplete = true
                    }
                }
                else -> Unit
            }
        }
    }*/
}
