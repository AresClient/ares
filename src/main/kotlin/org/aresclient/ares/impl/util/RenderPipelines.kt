package org.aresclient.ares.impl.util

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.UniformType
import net.minecraft.client.render.VertexFormats
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull


object RenderPipelines {
    val outline: RenderPipeline = RenderPipeline.builder()
        .withLocation(Identifier.of("ares", "pipeline/outline_blit"))
        .withVertexShader(Identifier.of("ares", "shaders/vert/outline.vert"))
        .withFragmentShader(Identifier.of("ares", "shaders/frag/outline.frag"))
        .withSampler("theTexture")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withColorWrite(true, false)
        .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
        .withUniform("viewportSize", UniformType.VEC2)
        .withUniform("lineWeight", UniformType.FLOAT)
        .withCull(false)
        .build()

    private val pipelines = listOf(outline)

    class PipelineReloader: SynchronousResourceReloader {
        override fun reload(manager: ResourceManager) {
            for(pipeline in pipelines) {
                RenderSystem.getDevice().precompilePipeline(pipeline) { id, _ ->
                    (if(id.namespace == "ares") RenderPipelines::class.java.getResourceAsStream("/assets/ares/${id.path}")
                    else manager.getResource(id).getOrNull()?.inputStream)?.reader()?.readLines()?.joinToString("\n")
                }
            }
        }
    }
}