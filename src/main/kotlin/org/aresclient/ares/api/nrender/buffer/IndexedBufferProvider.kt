package org.aresclient.ares.api.nrender.buffer

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.render.RenderLayer
import java.util.*

class IndexedBufferProvider {
    private val buffers: SequencedMap<RenderLayer, IndexedBuffer> = LinkedHashMap<RenderLayer, IndexedBuffer>()

    fun getBuffer(renderLayer: RenderLayer): IndexedBuffer {
        return buffers.getOrPut(renderLayer) { IndexedBuffer(renderLayer.drawMode, renderLayer.vertexFormat) }
    }

    fun draw() {
        buffers.entries.forEach { (layer, buffer) ->
            draw(layer, buffer)
        }
    }

    private fun draw(layer: RenderLayer, buffer: IndexedBuffer) {
        if(buffer.count() == 0) return

        val vertexBuffer = buffer.getVertexBuffer()
        val indexBuffer = buffer.getIndexBuffer()

        RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(
                layer.target.colorAttachment, OptionalInt.empty(),
                if(layer.target.useDepthAttachment) layer.target.depthAttachment else null, OptionalDouble.empty()
            )
            .use { pass ->
                pass.setPipeline(layer.pipeline)

                if(RenderSystem.SCISSOR_STATE.isEnabled)
                    pass.enableScissor(RenderSystem.SCISSOR_STATE)

                for(i in 0..11) {
                    val gpuTexture = RenderSystem.getShaderTexture(i)
                    if(gpuTexture != null) pass.bindSampler("Sampler$i", gpuTexture)
                }

                pass.setVertexBuffer(0, vertexBuffer)
                pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT)
                pass.drawIndexed(0, buffer.count())
            }

        buffer.reset()
    }
}
