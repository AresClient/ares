package org.aresclient.ares.impl.instrument.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.Entity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.EntityUtil.getTargetColor
import org.aresclient.ares.impl.util.EntityUtil.isTarget
import org.aresclient.ares.impl.util.RenderUtil
import org.aresclient.ares.impl.util.RenderPipelines
import java.util.*

// TODO: FIX DEPTH ON OUTLINE ESP
// TODO: MAKE THIS MORE CUSTOMIZABLE + FRIENDS
object ESP: Module(Category.RENDER, "ESP", "See outlines of entities through walls") {
    enum class Mode { OUTLINE, BOX }

    private val mode = settings.addEnum("Mode", Mode.OUTLINE)

    private val players = settings.addBoolean("Players", true)
    private val friends = settings.addBoolean("Friends", true).setVisibility(players::getValue)
    private val teammates = settings.addBoolean("Teammates", true).setVisibility(players::getValue)
    private val passive = settings.addBoolean("Passive", true)
    private val hostile = settings.addBoolean("Hostile", true)
    private val items = settings.addBoolean("Items", true)
    private val nametagged = settings.addBoolean("Nametagged", true)
    private val bots = settings.addBoolean("Bots", false)

    fun getEntityColor(entity: Entity) = entity.getTargetColor()
    fun shouldRenderOutline() = isEnabled() && mode.value == Mode.OUTLINE
    fun shouldRenderOutline(entity: Entity) =
        shouldRenderOutline() && entity.isTarget(
            players.value,friends.value, teammates.value, passive.value,
            hostile.value, items.value, nametagged.value, bots.value
        )

    override fun onRenderWorld(delta: Float, renderer: Renderer.State) {
        if(mode.value != Mode.BOX) return
        MC.world?.entities?.filter { it.isTarget(
                players.value,friends.value, teammates.value, passive.value,
                hostile.value, items.value, nametagged.value, bots.value
        ) }?.forEach { entity ->
           if(entity != MC.player) {
               val box = entity.getInterpolatedBoundingBox(delta)
               val color = getEntityColor(entity)
               RenderUtil.Lines.box(box, color, 1f)
               RenderUtil.Fill.box(box, color.deriveAlpha(0.2f))
           }
        }
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val camera = MC.gameRenderer.camera.pos
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x - camera.x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y - camera.y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z - camera.z
        return Box(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }

    // see MixinWorldRenderer.java
    object Outliner {
        val framebuffer = SimpleFramebuffer("Ares Outline Framebuffer", MC.framebuffer.textureWidth, MC.framebuffer.textureHeight, true)
        val vertexConsumerProvider = OutlineVertexConsumerProvider(MC.bufferBuilders.entityVertexConsumers)

        fun setColor(color: Color) {
            vertexConsumerProvider.setColor((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt(), 255) // TODO: ALPHA??
        }

        fun blit() {
            RenderSystem.assertOnRenderThread()
            val shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)
            val gpuBuffer = shapeIndexBuffer.getIndexBuffer(6)
            val gpuBuffer2 = RenderSystem.getQuadVertexBuffer()
            RenderSystem.getDevice().createCommandEncoder().createRenderPass(MC.framebuffer.colorAttachment, OptionalInt.empty())
            .use { renderPass ->
                renderPass.setPipeline(RenderPipelines.outline)
                renderPass.setVertexBuffer(0, gpuBuffer2)
                renderPass.setIndexBuffer(gpuBuffer, shapeIndexBuffer.indexType)
                renderPass.bindSampler("theTexture", framebuffer.colorAttachment)
                renderPass.setUniform("viewportSize", MC.framebuffer.textureWidth.toFloat(), MC.framebuffer.textureHeight.toFloat())
                renderPass.setUniform("lineWeight", 1f)
                renderPass.drawIndexed(0, 6)
            }
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(framebuffer.colorAttachment, 0, framebuffer.depthAttachment, 0.0)
        }
    }
}
