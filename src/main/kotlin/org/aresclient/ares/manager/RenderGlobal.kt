package org.aresclient.ares.manager

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.render.RenderEvent
import org.aresclient.ares.Ares
import org.aresclient.ares.Global
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer

class RenderGlobal: Global("Render Global") {
    enum class Buffers(val buffer: Lazy<Buffer>) {
        TRIANGLES(lazy { Buffer.beginDynamic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR) }),
        LINES(lazy { Buffer.beginDynamic(Shader.LINES, VertexFormat.LINES).lines() });

        fun get(): Buffer {
            return buffer.value
        }

        fun begin() {
            buffer.value.begin()
        }

        fun draw(matrixStack: MatrixStack) {
            buffer.value
                .end()
                .draw(matrixStack)
        }
    }

    @field:EventHandler
    private val renderEvent = EventListener<RenderEvent.World> { e ->
        Buffers.TRIANGLES.begin()
        val t = Ares.MESH.eventManager.post(RenderGlobalEvent(Buffers.TRIANGLES, e.tickDelta))
        Ares.MODULES.forEach { it.renderWorld(t) }
        Renderer.render3d(Buffers.TRIANGLES::draw)

        Buffers.LINES.begin()
        val l = Ares.MESH.eventManager.post(RenderGlobalEvent(Buffers.LINES, e.tickDelta))
        Ares.MODULES.forEach { it.renderWorld(l) }
        Renderer.render3d(Buffers.LINES::draw)
    }
}

class RenderGlobalEvent(val type: RenderGlobal.Buffers, val delta: Float): MeshEvent("ares-render-builder-" + type.name) {
    fun getBuffer(): Buffer = type.buffer.value
}