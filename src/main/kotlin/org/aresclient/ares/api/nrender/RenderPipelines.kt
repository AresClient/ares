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

    private val defaults = RenderPipeline.builder()
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .withUniform("ProjMat", UniformType.MATRIX4X4)
        .withUniform("ModelViewMat", UniformType.MATRIX4X4)
        .buildSnippet()

    private val pos_tex_color = RenderPipeline.builder(defaults)
        .withVertexShader(identifier("nshaders/vert/position_texture_color.vert"))
        .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
        .buildSnippet()

    val texture: RenderPipeline = RenderPipeline.builder(pos_tex_color)
        .withLocation(identifier("pipeline/texture"))
        .withFragmentShader(identifier("nshaders/frag/texture.frag"))
        .withSampler("Sampler0")
        .add()

    val ellipse: RenderPipeline = RenderPipeline.builder(pos_tex_color)
        .withLocation(identifier("pipeline/ellipse"))
        .withFragmentShader(identifier("nshaders/frag/ellipse.frag"))
        .add()

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
