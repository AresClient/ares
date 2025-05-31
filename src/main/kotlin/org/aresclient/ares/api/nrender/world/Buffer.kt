package org.aresclient.ares.api.nrender.world

import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode
import com.mojang.blaze3d.vertex.VertexFormatElement
import org.aresclient.ares.api.util.Color
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

open class Buffer(size: Int) {
    private var buffer = BufferUtils.createByteBuffer(size)
    private var count = 0

    fun pos(pos: Vector4f): Buffer {
        return floats(pos.x, pos.y, pos.z, pos.w)
    }

    fun pos(pos: Vector3f): Buffer {
        return floats(pos.x, pos.y, pos.z)
    }

    fun floats(vararg floats: Float): Buffer {
        buffer = expandBuffer(floats.size * VertexFormatElement.Type.FLOAT.size())
        for(vertex in floats) buffer.putFloat(vertex)
        return this
    }

    fun ints(vararg ints: Int): Buffer {
        buffer = expandBuffer(ints.size * VertexFormatElement.Type.INT.size())
        for(vertex in ints) buffer.putInt(vertex)
        return this
    }

    fun shorts(vararg shorts: Short): Buffer {
        buffer = expandBuffer(shorts.size * VertexFormatElement.Type.SHORT.size())
        for(vertex in shorts) buffer.putShort(vertex)
        return this
    }

    fun bytes(vararg bytes: Byte): Buffer {
        buffer = expandBuffer(bytes.size * VertexFormatElement.Type.BYTE.size())
        buffer.put(bytes)
        return this
    }

    fun color(color: Color): Buffer {
        bytes(toByte(color.red), toByte(color.green), toByte(color.blue), toByte(color.alpha))
        return this
    }

    private fun toByte(value: Float) = (value * 255.0).toInt().toByte()

    fun next(): Int {
        return count++
    }

    open fun count(): Int {
        return count
    }

    fun size(): Int {
        return buffer.position()
    }

    fun reset() {
        buffer.clear()
        count = 0
    }

    fun get(): ByteBuffer {
        buffer.limit(buffer.position())
        return buffer.flip()
    }

    class Vertex(vertexFormat: VertexFormat, count: Int): Buffer(count * vertexFormat.vertexSize)

    class Index(drawMode: DrawMode, count: Int): Buffer(count * drawMode.firstVertexCount * VertexFormatElement.Type.INT.size())

    private fun expandBuffer(count: Int): ByteBuffer {
        val needed = buffer.position() + count
        if(needed < buffer.capacity()) return buffer

        var size = buffer.capacity()
        while(size < needed) size *= 2

        val newBuffer = BufferUtils.createByteBuffer(size)
        newBuffer.put(buffer.flip())
        return newBuffer
    }
}
