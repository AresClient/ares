package org.aresclient.ares.api.nrender

import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.util.Identifier
import net.minecraft.util.TriState
import net.minecraft.util.Util
import java.util.function.Function

object AresRenderLayers {
    val QUAD_TEXTURE: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/quad_texture", 786432, AresRenderPipelines.QUAD_TEXTURE, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .build(false))
    }

    val QUAD_ELLIPSE: RenderLayer.MultiPhase = RenderLayer.of("hud/quad_ellipse", 1536, AresRenderPipelines.QUAD_ELLIPSE, RenderLayer.MultiPhaseParameters.builder().build(false))

    val QUAD_ROUNDED: RenderLayer.MultiPhase = RenderLayer.of("hud/quad_rounded", 1536, AresRenderPipelines.QUAD_ROUNDED, RenderLayer.MultiPhaseParameters.builder().build(false))

    val QUAD_NO_DEPTH: RenderLayer.MultiPhase = RenderLayer.of("hud/quad_no_depth", 1536, AresRenderPipelines.QUAD_NO_DEPTH, RenderLayer.MultiPhaseParameters.builder().build(false))

    val LINES: RenderLayer = RenderLayer.of("lines", 1536, AresRenderPipelines.LINES, RenderLayer.MultiPhaseParameters.builder().build(false))

    val LINES_NO_DEPTH: RenderLayer = RenderLayer.of("lines", 1536, AresRenderPipelines.LINES_NO_DEPTH, RenderLayer.MultiPhaseParameters.builder().build(false))

    val TEXT: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/text", 786432, false, false, RenderPipelines.RENDERTYPE_TEXT, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .build(false))
    }

    val TEXT_SEE_THROUGH: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/text_see_through", 1536, false, false, RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .build(false))
    }

    val TEXT_POLYGON_OFFSET: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/text_polygon_offset", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_POLYGON_OFFSET, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .build(false))
    }

    val TEXT_INTENSITY: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/text_intensity", 786432, false, false, RenderPipelines.RENDERTYPE_TEXT_INTENSITY, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .build(false))
    }

    val TEXT_INTENSITY_SEE_THROUGH: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/text_intensity_see_through", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_INTENSITY_SEETHROUGH, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .build(false))
    }

    val TEXT_INTENSITY_POLYGON_OFFSET: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("hud/text_intensity_polygon_offset", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_INTENSITY, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .build(false))
    }
}
