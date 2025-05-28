package org.aresclient.ares.api.nrender

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.util.Identifier
import net.minecraft.util.TriState
import net.minecraft.util.Util
import java.util.function.Function

object RenderLayers {
    init {
        Hud
        World
    }

    object Hud {
        val quad_texture: Function<Identifier, RenderLayer> = Util.memoize<Identifier, RenderLayer> {
            RenderLayer.of("quad_texture", 786432, RenderPipelines.Hud.quad_texture, RenderLayer.MultiPhaseParameters.builder()
                .texture(RenderPhase.Texture(it, TriState.TRUE, true))
                .build(false))
        }

        val quad_ellipse: RenderLayer.MultiPhase = RenderLayer.of("quad_ellipse", 1536, RenderPipelines.Hud.quad_ellipse, RenderLayer.MultiPhaseParameters.builder().build(false))

        val quad_rounded: RenderLayer.MultiPhase = RenderLayer.of("quad_rounded", 1536, RenderPipelines.Hud.quad_rounded, RenderLayer.MultiPhaseParameters.builder().build(false))
    }

    object World {
        val triangle_color: RenderLayer.MultiPhase = RenderLayer.of("triangle_color", 1536, RenderPipelines.World.triangle_color, RenderLayer.MultiPhaseParameters.builder().build(false))
    }
}
