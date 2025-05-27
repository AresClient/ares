package org.aresclient.ares.api.nrender.world

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode

class IndexedBuffer(drawMode: DrawMode, private val vertexFormat: VertexFormat) {
    companion object {
        private const val INITIAL_BUFFER_SIZE = 1024
    }

    private var count = 0
    private val indices = Buffer.Index(drawMode, INITIAL_BUFFER_SIZE)
    private val vertices = Buffer.Vertex(vertexFormat, INITIAL_BUFFER_SIZE)

    private fun indices(vararg ints: Int) {
        indices.ints(*ints)
    }

    fun quad(i0: Int, i1: Int, i2: Int, i3: Int) {
        count += 2
        return indices(i0, i1, i2, i2, i3, i0)
    }

    fun triangle(i0: Int, i1: Int, i2: Int) {
        count += 1
        return indices(i0, i1, i2)
    }

    fun vertices(use: Buffer.() -> Unit) {
        vertices.apply(use)
    }

    fun getVertexBuffer(): GpuBuffer {
        return vertexFormat.uploadImmediateVertexBuffer(vertices.get())
    }

    fun getIndexBuffer(): GpuBuffer {
        return vertexFormat.uploadImmediateIndexBuffer(indices.get())
    }

    fun count(): Int {
        return count
    }

    fun reset() {
        // TODO: count = 0
        indices.reset()
        vertices.reset()
    }
}
