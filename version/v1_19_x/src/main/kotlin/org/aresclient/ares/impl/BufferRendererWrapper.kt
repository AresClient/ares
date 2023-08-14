package org.aresclient.ares.impl

import org.aresclient.ares.api.BufferRenderer
import org.aresclient.ares.mixininterface.IBufferRenderer
import org.aresclient.ares.mixininterface.IVertexBuffer

object BufferRendererWrapper: BufferRenderer {
    // Instantiate to workaround Accessor bug for now
    private val br = net.minecraft.client.render.BufferRenderer()

    override fun getVertexArray(): Int = ((br as IBufferRenderer).getCurrentVertexBuffer() as IVertexBuffer).vertexArrayId
    override fun setVertexArray(vao: Int) {
        ((br as IBufferRenderer).getCurrentVertexBuffer() as IVertexBuffer).vertexArrayId = vao
    }

    override fun getVertexBuffer(): Int = ((br as IBufferRenderer).getCurrentVertexBuffer() as IVertexBuffer).vertexBufferId
    override fun setVertexBuffer(vbo: Int) {
        ((br as IBufferRenderer).getCurrentVertexBuffer() as IVertexBuffer).vertexBufferId = vbo
    }

    override fun getElementBuffer(): Int = ((br as IBufferRenderer).getCurrentVertexBuffer() as IVertexBuffer).indexBufferId
    override fun setElementBuffer(ibo: Int) {
        ((br as IBufferRenderer).getCurrentVertexBuffer() as IVertexBuffer).indexBufferId = ibo
    }
}