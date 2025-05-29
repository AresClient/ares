package org.aresclient.ares.api.nrender

import net.minecraft.client.font.FontStorage
import net.minecraft.client.font.TextRenderLayerSet
import net.minecraft.client.texture.TextureManager
import net.minecraft.util.Identifier

class CustomFontStorage(textureManager: TextureManager, id: Identifier): FontStorage(textureManager, id) {
    companion object {
        @JvmStatic
        fun renderLayerSetOf(id: Identifier): TextRenderLayerSet {
            return TextRenderLayerSet(
                AresRenderLayers.Hud.TEXT.apply(id),
                AresRenderLayers.Hud.TEXT_SEE_THROUGH.apply(id),
                AresRenderLayers.Hud.TEXT_POLYGON_OFFSET.apply(id)
            )
        }

        @JvmStatic
        fun renderLayerSetOfIntensity(id: Identifier): TextRenderLayerSet {
            return TextRenderLayerSet(
                AresRenderLayers.Hud.TEXT_INTENSITY.apply(id),
                AresRenderLayers.Hud.TEXT_INTENSITY_SEE_THROUGH.apply(id),
                AresRenderLayers.Hud.TEXT_INTENSITY_POLYGON_OFFSET.apply(id)
            )
        }
    }
}
