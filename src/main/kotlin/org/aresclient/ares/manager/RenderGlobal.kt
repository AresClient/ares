package org.aresclient.ares.manager

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.event.MeshEvent
import net.meshmc.mesh.event.events.render.RenderEvent
import org.aresclient.ares.Ares
import org.aresclient.ares.Global
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import java.util.*

class RenderGlobal: Global("Render Global") {
    companion object {
        lateinit var INSTANCE: RenderGlobal
    }
    
    init {
        INSTANCE = this
    }

    object TriangleOrder
    object LineOrder

    val triangles by lazy { Buffer.beginDynamic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR) }
    val lines by lazy { Buffer.beginDynamic(Shader.LINES, VertexFormat.LINES).lines() }

    val triangleOrders = LinkedList<() -> TriangleOrder>()
    val lineOrders = LinkedList<() -> LineOrder>()

    @field:EventHandler
    private val renderEvent = EventListener<RenderEvent.World> { e ->
        val event = Ares.MESH.eventManager.post(RenderGlobalEvent(e.tickDelta))
        Ares.MODULES.forEach { it.renderWorld(event) }
        
        triangles.begin()
        triangleOrders.forEach { it.invoke() }
        triangles.end()
        Renderer.render3d(triangles::draw)

        lines.begin()
        lineOrders.forEach { it.invoke() }
        lines.end()
        Renderer.render3d(lines::draw)

        triangleOrders.clear()
        lineOrders.clear()
    }
}

class RenderGlobalEvent(val delta: Float): MeshEvent("ares-render-builder") {
    fun triangles(order: () -> RenderGlobal.TriangleOrder) {
        RenderGlobal.INSTANCE.triangleOrders.add(order)
    }
    
    fun lines(order: () -> RenderGlobal.LineOrder) {
        RenderGlobal.INSTANCE.lineOrders.add(order)
    }
}