package org.aresclient.ares.impl.util

import org.aresclient.ares.api.render.FontRenderer
import java.awt.Font

// TODO: remove in favor of NFonts
enum class Fonts(private val font: Font) {
    ARIAL(Font.createFont(Font.TRUETYPE_FONT, Fonts::class.java.getResourceAsStream("/assets/ares/font/arial.ttf"))),
    MONO(Font.createFont(Font.TRUETYPE_FONT, Fonts::class.java.getResourceAsStream("/assets/ares/font/mono.ttf")));

    private val renderers: HashMap<Int, FontRenderer> = hashMapOf()

    fun getRenderer(style: Int = Font.PLAIN): FontRenderer {
        return renderers.getOrPut(style) { FontRenderer(font, style, 32f) }
    }
}
