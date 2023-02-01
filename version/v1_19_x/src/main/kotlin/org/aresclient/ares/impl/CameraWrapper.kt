package org.aresclient.ares.impl

import net.minecraft.client.MinecraftClient
import org.aresclient.ares.api.Camera
import org.aresclient.ares.api.math.Vec3d

object CameraWrapper: Camera {
    override fun getPos(): Vec3d = MinecraftClient.getInstance().gameRenderer.camera.pos as Vec3d

    override fun getX(): Double = MinecraftClient.getInstance().gameRenderer.camera.pos.x
    override fun getY(): Double = MinecraftClient.getInstance().gameRenderer.camera.pos.y
    override fun getZ(): Double = MinecraftClient.getInstance().gameRenderer.camera.pos.z

    override fun getPitch(): Float = MinecraftClient.getInstance().gameRenderer.camera.pitch
    override fun getYaw(): Float = MinecraftClient.getInstance().gameRenderer.camera.yaw
}