package org.aresclient.ares.api.nrender.hud

import net.minecraft.client.font.FontStorage
import net.minecraft.client.font.TextRenderLayerSet
import net.minecraft.client.texture.TextureManager
import net.minecraft.util.Identifier
import org.aresclient.ares.api.nrender.AresRenderLayers

class CustomFontStorage(textureManager: TextureManager, id: Identifier): FontStorage(textureManager, id) {
    companion object {
        @JvmStatic
        fun renderLayerSetOf(id: Identifier): TextRenderLayerSet {
            return TextRenderLayerSet(
                AresRenderLayers.TEXT.apply(id),
                AresRenderLayers.TEXT_SEE_THROUGH.apply(id),
                AresRenderLayers.TEXT_POLYGON_OFFSET.apply(id)
            )
        }

        @JvmStatic
        fun renderLayerSetOfIntensity(id: Identifier): TextRenderLayerSet {
            return TextRenderLayerSet(
                AresRenderLayers.TEXT_INTENSITY.apply(id),
                AresRenderLayers.TEXT_INTENSITY_SEE_THROUGH.apply(id),
                AresRenderLayers.TEXT_INTENSITY_POLYGON_OFFSET.apply(id)
            )
        }
    }
}
