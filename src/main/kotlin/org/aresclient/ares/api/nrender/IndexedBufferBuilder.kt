package org.aresclient.ares.api.nrender

import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BuiltBuffer
import net.minecraft.client.util.BufferAllocator
import org.aresclient.ares.mixin.accessors.AccessBufferBuilder
import org.lwjgl.system.MemoryUtil
import java.nio.ByteOrder

class IndexedBufferBuilder(private val indexAllocator: BufferAllocator, vertexAllocator: BufferAllocator, drawMode: VertexFormat.DrawMode, vertexFormat: VertexFormat): BufferBuilder(vertexAllocator, drawMode, vertexFormat) {
    companion object {
        private val LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
    }

    private var indexPointer = -1L

    // must be declared before vertices
    fun indices(vararg indices: Int) {
        indexPointer = indexAllocator.allocate(indices.size * VertexFormatElement.Type.INT.size())

        val offset = (this as AccessBufferBuilder).vertexCount
        for(i in indices) putInt(indexPointer, i + offset)
    }

    fun build(): Pair<BuiltBuffer, BufferAllocator.CloseableBuffer>? {
        val built = endNullable() ?: return null
        return Pair(built, indexAllocator.allocated!!)
    }

    private fun putInt(pointer: Long, i: Int) {
        if(LITTLE_ENDIAN) MemoryUtil.memPutInt(pointer, i)
        else {
            MemoryUtil.memPutShort(pointer, (i and 65535).toShort())
            MemoryUtil.memPutShort(pointer + 2L, (i shr 16 and 65535).toShort())
        }
    }
}
