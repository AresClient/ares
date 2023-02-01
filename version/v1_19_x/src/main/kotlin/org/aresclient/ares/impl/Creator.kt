package org.aresclient.ares.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.DemoScreen
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.realms.gui.screen.RealmsMainScreen
import net.minecraft.util.math.Direction
import org.aresclient.ares.api.ICreator
import org.aresclient.ares.api.math.*
import org.aresclient.ares.api.packet.ChatMessageC2SPacket
import org.aresclient.ares.api.packet.PlayerMoveC2SPacket.*
import org.aresclient.ares.mixins.ClientPlayNetworkHandlerAccessor
import java.time.Instant

class Creator: ICreator {
    override fun blockPos(x: Int, y: Int, z: Int): BlockPos {
        return net.minecraft.util.math.BlockPos(x, y, z) as BlockPos
    }

    override fun box(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Box {
        return net.minecraft.util.math.Box(x1, y1, z1, x2, y2, z2) as Box
    }

    val VALUES = Direction.values()
    override fun facing(id: Int): Facing {
        return VALUES[id] as Facing
    }

    override fun vec2f(x: Float, y: Float): Vec2f {
        return net.minecraft.util.math.Vec2f(x, y) as Vec2f
    }

    override fun vec3d(x: Double, y: Double, z: Double): Vec3d {
        return net.minecraft.util.math.Vec3d(x, y, z) as Vec3d
    }

    override fun vec3f(x: Float, y: Float, z: Float): Vec3f {
        return Vec3f(x, y, z)
    }

    override fun vec3i(x: Int, y: Int, z: Int): Vec3i {
        return net.minecraft.util.math.Vec3i(x, y, z) as Vec3i
    }

    override fun openChatScreen(input: String) {
        MinecraftClient.getInstance().setScreen(ChatScreen(input))
    }

    override fun openDemoScreen() {
        MinecraftClient.getInstance().setScreen(DemoScreen())
    }

    override fun openMultiplayerScreen() {
        MinecraftClient.getInstance().setScreen(MultiplayerScreen(MinecraftClient.getInstance().currentScreen))
    }

    override fun openOptionsScreen() {
        MinecraftClient.getInstance().setScreen(OptionsScreen(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance().options))
    }

    override fun openSelectWorldScreen() {
        MinecraftClient.getInstance().setScreen(SelectWorldScreen(MinecraftClient.getInstance().currentScreen))
    }

    override fun openRealmsMainScreen() {
        MinecraftClient.getInstance().setScreen(RealmsMainScreen(MinecraftClient.getInstance().currentScreen))
    }

    override fun openTitleScreen() {
        MinecraftClient.getInstance().setScreen(TitleScreen())
    }

    override fun createCPacketChatMessage(message: String, timestamp: Instant, salt: Long): ChatMessageC2SPacket {
        val a = (MinecraftClient.getInstance().player!!.networkHandler as ClientPlayNetworkHandlerAccessor).lastSeenMessagesCollector.collect()
        return net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket(message, timestamp, salt, null, a.update) as ChatMessageC2SPacket
    }

    override fun createPlayerMoveC2SPacketOnGround(onGround: Boolean): OnGround =
        net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly(onGround) as OnGround

    override fun createPlayerMoveC2SPacketPosition(x: Double, y: Double, z: Double, onGround: Boolean): Position =
        net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround) as Position

    override fun createPlayerMoveC2SPacketRotation(yaw: Float, pitch: Float, onGround: Boolean): Rotation =
        net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround) as Rotation

    override fun createPlayerMoveC2SPacketPositionRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, onGround: Boolean): PositionRotation =
        net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround) as PositionRotation
}