package org.aresclient.ares.impl.instrument.module.modules.render.esp

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.instrument.global.Render
import org.aresclient.ares.impl.util.Pipelines
import java.util.*

object ESP: Module(Category.RENDER, "ESP", "See outlines of players through walls") {
    enum class Mode { OUTLINE, BOX }

    private val mode = settings.addEnum("Mode", Mode.OUTLINE)
    private val thickness = settings.addFloat("Thickness", 1f).setMin(0.5f).setMax(4f)

    fun shouldRenderOutline() = isEnabled() && mode.value == Mode.OUTLINE

    // TODO: FIX NOT ALL ENTITIES IN RENDER DISTANCE VISIBLE ON OUTLINE MODE
    // TODO: FIX DEPTH ON OUTLINE ESP
    // TODO: MAKE THIS MORE CUSTOMIZABLE + FRIENDS
    // TODO: ALLOW ESP TO BE TURNED OFF FOR SPECIFIC ENTITIES
    fun getEntityColor(entity: Entity): Color {
        return when(entity) {
            is HostileEntity -> Color.RED
            is PlayerEntity -> Color.BLUE
            else -> Color.GREEN
        }
    }

    override fun onRenderWorld(delta:Float, buffers: Renderer.Buffers, matrixStack: MatrixStack) {
        if(mode.value != Mode.BOX) return
        MC.world?.entities?.forEach { entity ->
           if(entity != MC.player) {
               val box = entity.getInterpolatedBoundingBox(delta)
               val color = getEntityColor(entity)
               Render.Lines.box(box, color, thickness.value)
               Render.Fill.box(box, color.deriveAlpha(0.2f))
           }
        }
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z
        return Box(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }

    // see MixinWorldRenderer.java
    object Outliner {
        val framebuffer = SimpleFramebuffer("Ares Outline Framebuffer", MC.framebuffer.textureWidth, MC.framebuffer.textureHeight, false)
        val vertexConsumerProvider = OutlineVertexConsumerProvider(MC.bufferBuilders.entityVertexConsumers)

        fun setColor(color: Color) {
            vertexConsumerProvider.setColor((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt(), 255) // TODO: ALPHA??
        }

        fun blit() {
            RenderSystem.assertOnRenderThread()
            val shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)
            val gpuBuffer = shapeIndexBuffer.getIndexBuffer(6)
            val gpuBuffer2 = RenderSystem.getQuadVertexBuffer()
            RenderSystem.getDevice().createCommandEncoder().createRenderPass(MC.framebuffer.colorAttachment,
                OptionalInt.empty()
            ).use { renderPass ->
                renderPass.setPipeline(Pipelines.outline)
                renderPass.setVertexBuffer(0, gpuBuffer2)
                renderPass.setIndexBuffer(gpuBuffer, shapeIndexBuffer.indexType)
                renderPass.bindSampler("theTexture", framebuffer.colorAttachment)
                renderPass.setUniform("viewportSize", MC.framebuffer.textureWidth.toFloat(), MC.framebuffer.textureHeight.toFloat())
                renderPass.setUniform("lineWeight", thickness.value)
                renderPass.drawIndexed(0, 6)
            }
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(framebuffer.colorAttachment, 0)
        }
    }
}
