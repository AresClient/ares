package org.aresclient.ares.api.nrender.hud

import net.minecraft.client.font.Font
import net.minecraft.client.font.FontFilterType
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TrueTypeFontLoader
import net.minecraft.text.Text
import org.aresclient.ares.Ares
import org.aresclient.ares.api.Wrapper

enum class NFont(path: String?, val size: Float): Wrapper {
    MINECRAFT(null, 11f),
    ARIAL("arial.ttf", 32f),
    MONO("mono.ttf", 32f);

    val textRenderer: TextRenderer by lazy {
        if(path == null) MC.textRenderer
        else {
            val id = Ares.identifier(path)
            val storage = CustomFontStorage(MC.textureManager, Ares.identifier("fs_${path}"))
            storage.setFonts(mutableListOf(Font.FontFilterPair(TrueTypeFontLoader(id, size, 1f, TrueTypeFontLoader.Shift(0f, size / 5f), "").build().orThrow().load(MC.resourceManager), FontFilterType.FilterMap.NO_FILTER)), setOf())
            TextRenderer({ storage }, false)
        }
    }

    fun getScale(factor: Float): Float = factor / size

    fun getHeight(size: Float): Float {
        return textRenderer.fontHeight * getScale(size)
    }

    fun getWidth(text: Text, size: Float): Float {
        return textRenderer.getWidth(text) * getScale(size)
    }
}
