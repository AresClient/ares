package org.aresclient.ares.manager

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.api.math.Vec2f
import net.meshmc.mesh.api.packet.client.CPacketMovePlayer
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.client.PacketEvent
import org.aresclient.ares.Manager
import org.aresclient.ares.module.Module
import org.aresclient.ares.utils.Timer

object RotationManager: Manager() {
    private var rotation: Vec2f? = null

    private var key: Module? = null
    private var released = true
    private var resetTimer: Timer = Timer()

    fun getRotation(): Vec2f? {
        return rotation
    }

    fun keyMatches(module: Module): Boolean {
        return key == module
    }

    fun hasPriority(module: Module): Boolean {
        return key == null || (key as Module).priority < module.priority
    }

    fun getKeyPriority(): Int {
        if(key == null) return -1
        return key!!.priority
    }

    fun release(key: Module) {
        if(this.key == key) this.released = true
    }

    fun getReleased(): Boolean {
        return released
    }

    fun setRotation(yaw: Float, pitch: Float, key: Module): Boolean {
        return setRotation(yaw, pitch, key, false)
    }

    fun setRotation(yaw: Float, pitch: Float, key: Module, instant: Boolean): Boolean {
        return setRotation(Vec2f.create(yaw, pitch), key, instant)
    }

    fun setRotation(rotation: Vec2f, key: Module): Boolean {
        return setRotation(rotation, key, false)
    }

    fun setRotation(rotation: Vec2f, key: Module, instant: Boolean): Boolean {
        if(this.rotation == null || released || keyMatches(key) || hasPriority(key)) {
            // Rotate instantly if specified, but only if the rotation does not match (no need to spam)
            if(instant && this.rotation != rotation) CPacketMovePlayer.create(rotation, MC.player.isOnGround) //TODO: send packet (once possible in mesh)

            this.rotation = rotation
            this.key = key
            released = false
            resetTimer.reset()

            return true
        }

        return false
    }

    //TODO: switch to replacing EntityClientPlayerEvent.sendMovementPackets so that alternative packets can be sent without being modified?
    @field:EventHandler
    private val onMovementPacketSent = EventListener<PacketEvent.Sent> { event ->
        if(event.packet is CPacketMovePlayer && event.era == MeshEvent.Era.BEFORE) {
            if(rotation == null) {
                resetTimer.reset()
                return@EventListener
            }

            //TODO: Global settings (separate panel?)
            if(resetTimer.passedTicks(10)) {
                rotation = null
                key = null
                released = true
                resetTimer.reset()
                return@EventListener
            }

            (event.packet as CPacketMovePlayer).rotation = rotation

            MC.player.setHeadYaw(rotation!!.x)
            MC.player.setBodyYaw(rotation!!.x)
        }
    }
}