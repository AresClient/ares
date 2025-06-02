package org.aresclient.ares.impl.instrument.modules.render.esp

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.Entity
import org.aresclient.ares.api.nrender.AresRenderPipelines
import org.aresclient.ares.api.util.Color
import java.util.*

// see MixinWorldRenderer.java
object OutlineESP: ESP.RenderMode() {
    val framebuffer by lazy { SimpleFramebuffer("Ares Outline Framebuffer", MC.framebuffer.textureWidth, MC.framebuffer.textureHeight, true) }
    val vertexConsumerProvider by lazy { OutlineVertexConsumerProvider(MC.bufferBuilders.entityVertexConsumers) }

    private var shouldRenderOutlineCache: Boolean? = null

    override fun onTick() {
        shouldRenderOutlineCache = null
    }

    fun shouldRenderOutline(): Boolean {
        if(!ESP.isEnabled()) return false
        if(shouldRenderOutlineCache != null) return shouldRenderOutlineCache!!
        shouldRenderOutlineCache = WORLD.entities.any { entity -> ESP.getEntityGroup(entity)?.let { it.enabled.value && it.mode.value == ESP.Mode.OUTLINE } == true }
        return shouldRenderOutlineCache!!
    }

    fun shouldRenderOutline(entity: Entity) = shouldRenderOutline() && ESP.getEntityGroup(entity)?.let { it.enabled.value && it.mode.value == ESP.Mode.OUTLINE } == true

    fun getLineColor(entity: Entity): Color = ESP.getEntityGroup(entity)?.lineColor?.value ?: Color.COLORLESS

    fun setColor(color: Color) {
        vertexConsumerProvider.setColor((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt(), 255) // TODO: ALPHA??
    }

    fun blit() {
        RenderSystem.assertOnRenderThread()
        val shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)
        val gpuBuffer = shapeIndexBuffer.getIndexBuffer(6)
        val gpuBuffer2 = RenderSystem.getQuadVertexBuffer()
        RenderSystem.getDevice()
            .createCommandEncoder().createRenderPass(MC.framebuffer.colorAttachment, OptionalInt.empty())
        .use { renderPass ->
            renderPass.setPipeline(AresRenderPipelines.OUTLINE)
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
