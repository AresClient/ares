package org.aresclient.ares.api.nrender

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gl.UniformType
import net.minecraft.client.render.VertexFormats
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import org.aresclient.ares.Ares
import kotlin.jvm.optionals.getOrNull

object AresRenderPipelines {
    private val pipelines = mutableListOf<RenderPipeline>()

    private val defaults = RenderPipeline.builder()
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .withUniform("ProjMat", UniformType.MATRIX4X4)
        .withUniform("ModelViewMat", UniformType.MATRIX4X4)
        .buildSnippet()

    val QUAD_TEXTURE: RenderPipeline = RenderPipeline.builder(defaults)
        .withLocation(Ares.identifier("pipeline/quad_texture"))
        .withVertexShader(Ares.identifier("nshaders/vert/pos_uv_color.vert"))
        .withFragmentShader(Ares.identifier("nshaders/frag/texture.frag"))
        .withSampler("Sampler0")
        .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
        .add()

    val QUAD_ELLIPSE: RenderPipeline = RenderPipeline.builder(defaults)
        .withLocation(Ares.identifier("pipeline/quad_ellipse"))
        .withVertexShader(Ares.identifier("nshaders/vert/pos_uv_color.vert"))
        .withFragmentShader(Ares.identifier("nshaders/frag/ellipse.frag"))
        .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
        .add()

    val QUAD_ROUNDED: RenderPipeline = RenderPipeline.builder(defaults)
        .withLocation(Ares.identifier("pipeline/quad_rounded"))
        .withVertexShader(Ares.identifier("nshaders/vert/pos_color_norm.vert"))
        .withFragmentShader(Ares.identifier("nshaders/frag/rounded.frag"))
        .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.QUADS)
        .add()

    val QUAD_NO_DEPTH: RenderPipeline = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Ares.identifier("pipeline/quad_no_depth"))
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false)
        .add()

    private val lines_defaults: RenderPipeline.Snippet = RenderPipeline.builder(defaults)
        .withVertexShader(Ares.identifier("nshaders/vert/lines.vert"))
        .withFragmentShader(Ares.identifier("nshaders/frag/lines.frag"))
        .withUniform("AARadius", UniformType.FLOAT)
        .withVertexFormat(AresVertexFormats.LINES, VertexFormat.DrawMode.TRIANGLES)
        .buildSnippet()

    val LINES: RenderPipeline = RenderPipeline.builder(lines_defaults)
        .withLocation(Ares.identifier("pipeline/lines"))
        .add()

    val LINES_NO_DEPTH: RenderPipeline = RenderPipeline.builder(lines_defaults)
        .withLocation(Ares.identifier("pipeline/lines_no_depth"))
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .add()

    val OUTLINE: RenderPipeline = RenderPipeline.builder()
        .withLocation(Ares.identifier("pipeline/outline"))
        .withVertexShader(Ares.identifier("shaders/vert/outline.vert"))
        .withFragmentShader(Ares.identifier("shaders/frag/outline.frag"))
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

    private fun RenderPipeline.Builder.add(): RenderPipeline {
        val pipeline = build()
        pipelines.add(pipeline)
        return pipeline
    }

    class PipelineReloader: SynchronousResourceReloader {
        override fun reload(manager: ResourceManager) {
            for(pipeline in pipelines) {
                RenderSystem.getDevice().precompilePipeline(pipeline) { id, _ ->
                    (if(id.namespace == Ares.MODID) AresRenderPipelines::class.java.getResourceAsStream("/assets/ares/${id.path}")
                    else manager.getResource(id).getOrNull()?.inputStream)?.reader()?.readLines()?.joinToString("\n")
                }
            }
        }
    }
}
