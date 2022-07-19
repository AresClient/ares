package org.aresclient.ares.global

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.api.math.Vec2f
import net.meshmc.mesh.api.packet.client.CPacketMovePlayer
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.client.PacketEvent
import org.aresclient.ares.utils.Timer
import kotlin.math.min

/**
 * TODO: Conditional priority? (so that things don't always get overtaken by higher priority while executing)
 * TODO: Test whether interaction packets have to be sent right after the final step packet,
 *      at the end of the tick the packet's sent, or during the beginning of the next tick on strict;
 *      if it has to be the next tick, how soon after the interaction can the rotation be drastically changed
 */
interface Rotator {
    fun getPriority(): Int
    fun getYawStep(): Float = RotationGlobal.yaw_step.value
    fun getPitchStep(): Float = RotationGlobal.pitch_step.value
    fun isEmergencyInterruptor(): Boolean = false
}

object RotationGlobal: Global("Rotation") {
    val reset_delay = settings.long("Reset Delay", 10 , 0, 100)
    val yaw_step = settings.float("Yaw Step", 180F)
    val pitch_step = settings.float("Pitch Step", 180F)

    private var rotation: Vec2f? = null

    private var key: Rotator? = null
    private var released = true
    private var resetTimer: Timer = Timer()

    fun getRotation(): Vec2f? {
        return rotation?.duplicate()
    }

    fun keyMatches(rotator: Rotator): Boolean {
        return key == rotator
    }

    fun hasPriority(rotator: Rotator): Boolean {
        return key == null || key!!.getPriority() < rotator.getPriority()
    }

    fun getKeyPriority(): Int {
        if(key == null) return -1
        return key!!.getPriority()
    }

    fun release(key: Rotator) {
        if(this.key == key) this.released = true
    }

    fun getReleased(): Boolean {
        return released
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
        if(this.rotation == null || released || keyMatches(key) || hasPriority(key)) {
            // Rotate instantly if specified, but only if the rotation does not match (no need to spam)
            if(instant && this.rotation != rotation) CPacketMovePlayer.create(rotation, MC.player.isOnGround) //TODO: send packet (once possible in mesh)

            this.rotation = rotation
            this.key = key
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
    @field:EventHandler
    private val onMovementPacketSent = EventListener<PacketEvent.Sent> { event ->
        if(event.packet is CPacketMovePlayer) {
            val packet: CPacketMovePlayer = event.packet as CPacketMovePlayer
            when(event.era) {
                MeshEvent.Era.BEFORE -> {
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

                        MC.player.setRenderHeadYaw(tempRotation.x)
                        MC.player.setRenderBodyYaw(tempRotation.x)
                    } else {
                        packet.rotation = rotation
                        steppingComplete = true

                        MC.player.setRenderHeadYaw(rotation!!.x)
                        MC.player.setRenderBodyYaw(rotation!!.x)
                    }
                }
                MeshEvent.Era.AFTER -> {
                    lastRotation.set(packet.rotation)
                    rotation?.let {
                        if(lastRotation.x == it.x && lastRotation.y == it.y) steppingComplete = true
                    }
                }
                else -> Unit
            }
        }
    }
}
