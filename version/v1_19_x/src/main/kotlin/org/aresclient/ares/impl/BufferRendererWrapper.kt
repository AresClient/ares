package org.aresclient.ares.impl

import org.aresclient.ares.api.BufferRenderer
import org.aresclient.ares.mixins.BufferRendererAccessor
import org.aresclient.ares.mixins.VertexBufferAccessor

object BufferRendererWrapper: BufferRenderer {
    override fun getVertexArray(): Int = (BufferRendererAccessor.getCurrentVertexBuffer() as VertexBufferAccessor).vertexArrayId
    override fun setVertexArray(vao: Int) {
        (BufferRendererAccessor.getCurrentVertexBuffer() as VertexBufferAccessor).vertexArrayId = vao
    }

    override fun getVertexBuffer(): Int = (BufferRendererAccessor.getCurrentVertexBuffer() as VertexBufferAccessor).vertexBufferId
    override fun setVertexBuffer(vbo: Int) {
        (BufferRendererAccessor.getCurrentVertexBuffer() as VertexBufferAccessor).vertexBufferId = vbo
    }

    override fun getElementBuffer(): Int = (BufferRendererAccessor.getCurrentVertexBuffer() as VertexBufferAccessor).indexBufferId
    override fun setElementBuffer(ibo: Int) {
        (BufferRendererAccessor.getCurrentVertexBuffer() as VertexBufferAccessor).indexBufferId = ibo
    }
}