package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.EntityUtil.isFriend
import org.aresclient.ares.impl.util.EntityUtil.isTarget
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f

object Tracers: Module(Category.RENDER, "Tracers", "Render lines showing entities in render distance") {
    private val distance = settings.addBoolean("Distance", true)

    // TODO: switch to grouped element
    private val players = settings.addBoolean("Players", true)
    private val friends = settings.addBoolean("Friends", true).setVisibility(players::getValue)
    private val teammates = settings.addBoolean("Teammates", true).setVisibility(players::getValue)
    private val passive = settings.addBoolean("Passive", true)
    private val hostile = settings.addBoolean("Hostile", true)
    private val items = settings.addBoolean("Items", true)
    private val nametagged = settings.addBoolean("Nametagged", true)
    private val bots = settings.addBoolean("Bots", false)

    override fun onRenderWorld2d(delta: Float, renderer: Renderer.State, projection: Matrix4f) {
        val center = Vector2f(MC.window.framebufferWidth.toFloat(), MC.window.framebufferHeight.toFloat()).div(2f)

        MC.world?.entities?.filter { it.isTarget(
                players.value, friends.value, teammates.value, passive.value,
                hostile.value, items.value, nametagged.value, bots.value
        ) }?.forEach { entity ->
            val pos = entity.getLerpedRenderPos(delta)
            val color = if(entity.isFriend()) Color.rainbow() else (if(distance.value) Color.fromDistance(MC.player!!.distanceTo(entity)) else Color.WHITE)
            renderer.tryDrawTracer(projection, center, pos, pos.add(0.0, entity.height.toDouble(), 0.0), 1f, color)
        }
    }

    private fun Vec3d.toScreenPos(projection: Matrix4f): Vector2f? {
        val pos = Vector4f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), 1f).mul(projection)
        if(pos.w <= 0f) return null
        pos.div(pos.w)
        return Vector2f((pos.x + 1f) * MC.window.framebufferWidth.toFloat() * 0.5f, MC.window.framebufferHeight.toFloat() - (pos.y + 1f) * MC.window.framebufferHeight.toFloat() * 0.5f)
    }

    private fun Entity.getLerpedRenderPos(delta: Float): Vec3d = getLerpedPos(delta).subtract(MC.gameRenderer.camera.pos)

    private fun Renderer.State.tryDrawTracer(projection: Matrix4f, center: Vector2f, one: Vec3d, two: Vec3d, width: Float, color: Color) {
        drawTracer(center, one.toScreenPos(projection) ?: return, two.toScreenPos(projection) ?: return, width, color)
    }

    private fun Renderer.State.drawTracer(center: Vector2f, one: Vector2f, two: Vector2f, width: Float, color: Color) {
        buffers.lines.indicesOffset(0, 1, 1, 2)
        buffers.lines.vertices(
            center.x, center.y, 1f, width, color.red, color.green, color.blue, color.alpha,
            one.x, one.y, 1f, width, color.red, color.green, color.blue, color.alpha,
            two.x, two.y, 1f, width, color.red, color.green, color.blue, color.alpha,
        )
    }
}
