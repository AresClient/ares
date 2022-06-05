package org.aresclient.ares.module.render

import net.meshmc.mesh.api.entity.EntityType
import net.meshmc.mesh.api.math.Box
import net.meshmc.mesh.api.render.Renderer
import net.meshmc.mesh.util.render.Color
import net.meshmc.mesh.util.render.GlState
import net.meshmc.mesh.util.render.Vertex
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module

object ESP: Module("ESP", "See outlines of players through walls", Category.RENDER, enabled = true) {
    private val color = settings.color("Color", Color.RED)

    override fun renderWorld() {
        RENDERER.prepare3d()
        RENDERER.bufferBuilder.begin(Renderer.DrawMode.LINES, Renderer.VertexFormat.LINES)

        MC.world.loadedEntities.forEach { entity ->
            if(entity.entityType == EntityType.PLAYER && entity != MC.player)
                drawBox(entity.boundingBox, color.value)
        }

        RENDERER.bufferBuilder.draw()
        RENDERER.end3d()
    }

    private fun Renderer.prepare3d() {
        renderStack.push()
        renderStack.translate(RENDERER.camera)
        renderState
            .blend(true)
            .blendFunc(GlState.SourceFactor.SRC_ALPHA, GlState.DestFactor.ONE_MINUS_SRC_ALPHA)
            .depth(false)
            .alpha(false)
            .lineSmooth(true)
            .texture(false)
            .depthMask(false)
            .lineWeight(2f)
            .cull(false)
    }

    private fun Renderer.end3d() {
        renderState
            .depthMask(true)
            .depth(true)
            .texture(true)
            .alpha(true)
            .lineSmooth(false)
            .blend(false)
            .lineWeight(1f)
            .cull(true)
        renderStack.pop()
    }

   private fun drawBox(box: Box, color: Color) {
       RENDERER.bufferBuilder.construct(
           Vertex(box.minX, box.minY, box.minZ, color), Vertex(box.maxX, box.minY, box.minZ, color),
           Vertex(box.maxX, box.minY, box.minZ, color), Vertex(box.maxX, box.minY, box.maxZ, color),
           Vertex(box.maxX, box.minY, box.maxZ, color), Vertex(box.minX, box.minY, box.maxZ, color),
           Vertex(box.minX, box.minY, box.maxZ, color), Vertex(box.minX, box.minY, box.minZ, color),
           Vertex(box.minX, box.minY, box.minZ, color), Vertex(box.minX, box.maxY, box.minZ, color),
           Vertex(box.maxX, box.minY, box.minZ, color), Vertex(box.maxX, box.maxY, box.minZ, color),
           Vertex(box.maxX, box.minY, box.maxZ, color), Vertex(box.maxX, box.maxY, box.maxZ, color),
           Vertex(box.minX, box.minY, box.maxZ, color), Vertex(box.minX, box.maxY, box.maxZ, color),
           Vertex(box.minX, box.maxY, box.minZ, color), Vertex(box.maxX, box.maxY, box.minZ, color),
           Vertex(box.maxX, box.maxY, box.minZ, color), Vertex(box.maxX, box.maxY, box.maxZ, color),
           Vertex(box.maxX, box.maxY, box.maxZ, color), Vertex(box.minX, box.maxY, box.maxZ, color),
           Vertex(box.minX, box.maxY, box.maxZ, color), Vertex(box.minX, box.maxY, box.minZ, color)
       )
   }
}
