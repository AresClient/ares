package org.aresclient.ares.api.nrender

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.world.IndexedBufferProvider
import org.aresclient.ares.api.util.Color

open class Drawer: Wrapper {
    companion object: Wrapper {
        private val VERTEX_CONSUMERS = MC.bufferBuilders.entityVertexConsumers
        private val INDEXED_BUFFERS = IndexedBufferProvider()
    }

    protected val vertexConsumers: VertexConsumerProvider.Immediate = VERTEX_CONSUMERS
    protected val indexedBuffers: IndexedBufferProvider = INDEXED_BUFFERS

    protected fun VertexConsumer.color(color: Color): VertexConsumer = color(color.red, color.green, color.blue, color.alpha)

    fun draw() {
        vertexConsumers.draw()
        indexedBuffers.draw()
    }
}
