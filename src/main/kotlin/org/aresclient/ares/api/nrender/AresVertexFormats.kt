package org.aresclient.ares.api.nrender

import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement

object AresVertexFormats {
    private object AresVertexFormatElements {
        private val start = lastIndex()

        val CLIP_POSITION: VertexFormatElement = VertexFormatElement.register(start, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 4)
        val DISTANCES: VertexFormatElement = VertexFormatElement.register(start + 1, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4) // needs to have color usage so that its normalized

        private fun lastIndex(): Int {
            for(i in 0 until 32) {
                if(VertexFormatElement.byId(i) == null) return i
            }
            throw RuntimeException("Unable to create new VertexFormatElement")
        }
    }

    val LINES: VertexFormat = VertexFormat.builder()
        .add("Clip Position", AresVertexFormatElements.CLIP_POSITION)
        .add("Color", VertexFormatElement.COLOR)
        .add("Distances", AresVertexFormatElements.DISTANCES)
        .build()
}
