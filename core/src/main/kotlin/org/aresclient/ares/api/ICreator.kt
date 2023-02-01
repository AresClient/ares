package org.aresclient.ares.api

import org.aresclient.ares.api.math.*
import org.aresclient.ares.api.packet.ChatMessageC2SPacket
import org.aresclient.ares.api.packet.PlayerMoveC2SPacket
import java.time.Instant

interface ICreator {
    fun blockPos(x: Int, y: Int, z: Int): BlockPos

    fun box(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Box

    fun facing(id: Int): Facing

    fun vec2f(x: Float, y: Float): Vec2f

    fun vec3d(x: Double, y: Double, z: Double): Vec3d

    fun vec3f(x: Float, y: Float, z: Float): Vec3f

    fun vec3i(x: Int, y: Int, z: Int): Vec3i

    fun openChatScreen(input: String)

    fun openDemoScreen()

    fun openMultiplayerScreen()

    fun openOptionsScreen()

    fun openSelectWorldScreen()

    fun openRealmsMainScreen()

    fun openTitleScreen()

    fun createCPacketChatMessage(message: String, timestamp: Instant, salt: Long): ChatMessageC2SPacket

    fun createPlayerMoveC2SPacketOnGround(onGround: Boolean): PlayerMoveC2SPacket.OnGround
    fun createPlayerMoveC2SPacketPosition(x: Double, y: Double, z: Double, onGround: Boolean): PlayerMoveC2SPacket.Position
    fun createPlayerMoveC2SPacketRotation(yaw: Float, pitch: Float, onGround: Boolean): PlayerMoveC2SPacket.Rotation
    fun createPlayerMoveC2SPacketPositionRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, onGround: Boolean): PlayerMoveC2SPacket.PositionRotation
}