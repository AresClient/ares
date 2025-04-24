package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.EntityUtil.isFriend
import org.aresclient.ares.impl.util.EntityUtil.isTarget
import org.joml.Vector3f

object Tracers: Module(Category.RENDER, "Tracers", "Render lines showing entities in render distance") {
    private val distance = settings.addBoolean("Distance", true)

    private val players = settings.addBoolean("Players", true)
    private val friends = settings.addBoolean("Friends", true).setVisibility(players::getValue)
    private val teammates = settings.addBoolean("Teammates", true).setVisibility(players::getValue)
    private val passive = settings.addBoolean("Passive", true)
    private val hostile = settings.addBoolean("Hostile", true)
    private val items = settings.addBoolean("Items", true)
    private val nametagged = settings.addBoolean("Nametagged", true)
    private val bots = settings.addBoolean("Bots", false)

    override fun onRenderWorld(delta: Float, renderer: Renderer.State) {
        val camera = MC.gameRenderer.camera
        val eyes = Vec3d(0.0, 0.0, 1.0)
            .rotateX(-Renderer.toRadians(camera.pitch))
            .rotateY(-Renderer.toRadians(camera.yaw))

        MC.world?.entities?.filter { it.isTarget(
                players.value, friends.value, teammates.value, passive.value,
                hostile.value, items.value, nametagged.value, bots.value
        ) }?.forEach { entity ->
            val pos = entity.getLerpedRenderPos(delta)
            val color = if(entity.isFriend()) Color.rainbow() else (if(distance.value) Color.fromDistance(MC.player!!.distanceTo(entity)) else Color.WHITE)
            renderer.drawTracer(eyes, pos, pos.add(0.0, entity.height.toDouble(), 0.0), 1f, color)
        }
    }

    private fun Entity.getLerpedRenderPos(delta: Float): Vec3d = getLerpedPos(delta).subtract(MC.gameRenderer.camera.pos)

    private fun Renderer.State.drawTracer(one: Vec3d, two: Vec3d, three: Vec3d, width: Float, color: Color) =
        drawTracer(one.toVector3f(), two.toVector3f(), three.toVector3f(), width, color)

    private fun Renderer.State.drawTracer(one: Vector3f, two: Vector3f, three: Vector3f, width: Float, color: Color) {
        buffers.lines.indicesOffset(0, 1, 1, 2)
        buffers.lines.vertices(
            one.x, one.y, one.z, width, color.red, color.green, color.blue, color.alpha,
            two.x, two.y, two.z, width, color.red, color.green, color.blue, color.alpha,
            three.x, three.y, three.z, width, color.red, color.green, color.blue, color.alpha,
        )
    }
}
