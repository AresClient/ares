package org.aresclient.ares.api.nrender

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.util.Identifier
import net.minecraft.util.TriState
import net.minecraft.util.Util

object RenderLayers {
    val texture = Util.memoize<Identifier, RenderLayer> {
        RenderLayer.of("texture", 786432, RenderPipelines.texture, RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(it, TriState.TRUE, true))
            .build(false))
    }

    val ellipse = RenderLayer.of("ellipse", 1536, RenderPipelines.ellipse, RenderLayer.MultiPhaseParameters.builder().build(false))
}
