package org.aresclient.ares.api.nrender.drawer

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.BufferAllocator
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.buffer.IndexedBufferProvider
import org.aresclient.ares.api.util.Color

open class Drawer: Wrapper {
    protected val vertexConsumers = VertexConsumerProvider.immediate(BufferAllocator(1024))
    protected val indexedBuffers = IndexedBufferProvider()

    protected fun VertexConsumer.color(color: Color): VertexConsumer = color(color.red, color.green, color.blue, color.alpha)

    open fun begin() {
    }

    fun draw() {
        vertexConsumers.draw()
        indexedBuffers.draw()
    }
}
