package org.aresclient.ares.api.nrender

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
import org.aresclient.ares.Ares
import kotlin.jvm.optionals.getOrNull

object RenderPipelines {
    private val pipelines = mutableListOf<RenderPipeline>()

    init {
        Hud
        World
    }

    object Hud {
        private val defaults = RenderPipeline.builder()
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .buildSnippet()

        val quad_texture: RenderPipeline = RenderPipeline.builder(defaults)
            .withLocation(identifier("pipeline/hud/quad_texture"))
            .withVertexShader(identifier("nshaders/vert/hud/pos_tex_color.vert"))
            .withFragmentShader(identifier("nshaders/frag/texture.frag"))
            .withSampler("Sampler0")
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .add()

        val quad_ellipse: RenderPipeline = RenderPipeline.builder(defaults)
            .withLocation(identifier("pipeline/hud/quad_ellipse"))
            .withVertexShader(identifier("nshaders/vert/hud/pos_tex_color.vert"))
            .withFragmentShader(identifier("nshaders/frag/ellipse.frag"))
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .add()
    }

    object World {
        private val defaults = RenderPipeline.builder()
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withUniform("ProjMatWorld", UniformType.MATRIX4X4)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .buildSnippet()

        val triangle_color = RenderPipeline.builder(defaults)
            .withLocation(identifier("pipeline/world/triangle_color"))
            .withVertexShader(identifier("nshaders/vert/world/pos_color.vert"))
            .withFragmentShader(identifier("nshaders/frag/color.frag"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
            .add()
    }

    val outline: RenderPipeline = RenderPipeline.builder()
        .withLocation(identifier("pipeline/outline"))
        .withVertexShader(identifier("shaders/vert/outline.vert"))
        .withFragmentShader(identifier("shaders/frag/outline.frag"))
        .withSampler("theTexture")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withColorWrite(true, false)
        .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
        .withUniform("viewportSize", UniformType.VEC2)
        .withUniform("lineWeight", UniformType.FLOAT)
        .withCull(false)
        .add()

    private fun identifier(path: String): Identifier {
        return Identifier.of(Ares.MODID, path)
    }

    private fun RenderPipeline.Builder.add(): RenderPipeline {
        val pipeline = build()
        pipelines.add(pipeline)
        return pipeline
    }

    class PipelineReloader: SynchronousResourceReloader {
        override fun reload(manager: ResourceManager) {
            for(pipeline in pipelines) {
                RenderSystem.getDevice().precompilePipeline(pipeline) { id, _ ->
                    (if(id.namespace == Ares.MODID) RenderPipelines::class.java.getResourceAsStream("/assets/ares/${id.path}")
                    else manager.getResource(id).getOrNull()?.inputStream)?.reader()?.readLines()?.joinToString("\n")
                }
            }
        }
    }
}
