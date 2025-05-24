package org.aresclient.ares.api.nrender

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat.IndexType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.BufferAllocator
import java.util.*

// TODO: close buffer allocations on destroy
class IndexedVertexConsumerProvider {
    private val vertexBuffers: HashMap<RenderLayer, BufferAllocator> = hashMapOf()
    private val indexBuffers: HashMap<RenderLayer, BufferAllocator> = hashMapOf()

    private val pending: MutableMap<RenderLayer, IndexedBufferBuilder> = hashMapOf()

    fun getBuffer(renderLayer: RenderLayer): IndexedBufferBuilder {
        var bufferBuilder = pending[renderLayer]
        if(bufferBuilder != null) return bufferBuilder

        val vertexBuffer = vertexBuffers.getOrPut(renderLayer) { BufferAllocator(renderLayer.expectedBufferSize) }
        val indexBuffer = indexBuffers.getOrPut(renderLayer) { BufferAllocator(renderLayer.drawMode.getIndexCount(renderLayer.expectedBufferSize)) }
        bufferBuilder = IndexedBufferBuilder(indexBuffer, vertexBuffer, renderLayer.drawMode, renderLayer.vertexFormat)

        pending[renderLayer] = bufferBuilder
        return bufferBuilder
    }

    fun draw() {
        vertexBuffers.keys.forEach(this::draw)
    }

    fun draw(layer: RenderLayer) {
        pending.remove(layer)?.also { draw(layer, it) }
    }

    private fun draw(layer: RenderLayer, builder: IndexedBufferBuilder) {
        val buffers = builder.build() ?: return

        val vertBuffer = layer.pipeline.vertexFormat.uploadImmediateVertexBuffer(buffers.first.buffer)
        val indexBuffer = layer.pipeline.vertexFormat.uploadImmediateIndexBuffer(buffers.second.buffer)

        RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(
                layer.target.colorAttachment, OptionalInt.empty(),
                if(layer.target.useDepthAttachment) layer.target.depthAttachment else null, OptionalDouble.empty()
            ).use { renderPass ->
                renderPass.setPipeline(layer.pipeline)

                if(RenderSystem.SCISSOR_STATE.isEnabled)
                    renderPass.enableScissor(RenderSystem.SCISSOR_STATE)

                for(i in 0..11) {
                    val gpuTexture = RenderSystem.getShaderTexture(i)
                    if(gpuTexture != null) renderPass.bindSampler("Sampler$i", gpuTexture)
                }

                renderPass.setVertexBuffer(0, vertBuffer)
                renderPass.setIndexBuffer(indexBuffer, IndexType.INT)
                renderPass.drawIndexed(0, indexBuffer.size() / IndexType.INT.size)
            }
    }
}
