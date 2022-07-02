package org.aresclient.ares.global

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.event.events.render.RenderEvent
import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer

object RenderGlobal: Global("Render Global") {
    data class Event(val delta: Float, val matrixStack: MatrixStack, val buffers: Buffers)
    data class Buffers(val positionColor: Buffer, val lines: Buffer)

    private val buffers by lazy {
        Buffers(
            Buffer.createDynamic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR),
            Buffer.createDynamic(Shader.LINES, VertexFormat.LINES).lines()
        )
    }

    @field:EventHandler
    private val renderWorldEvent = EventListener<RenderEvent.World> { e ->
        Renderer.render3d { matrixStack ->
            buffers.positionColor.reset()
            buffers.lines.reset()

            val event = Ares.MESH.eventManager.post(Event(e.tickDelta, matrixStack, buffers))
            Ares.MODULES.forEach { it.renderWorld(event) }

            buffers.positionColor.draw(matrixStack)
            buffers.lines.draw(matrixStack)
        }
    }
}
