package dev.tigr.ares.fabric.impl.render;

import dev.tigr.ares.core.util.render.font.AbstractFontRenderer;
import dev.tigr.ares.core.util.render.font.AbstractGlyphPage;
import dev.tigr.ares.core.util.render.font.GlyphFont;

import java.awt.*;
import java.io.IOException;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermouthbear 10/3/20
 */
public class CustomFontRenderer extends AbstractFontRenderer {
    public CustomFontRenderer(GlyphFont glyphFont) {
        super(glyphFont);
    }

    @Override
    protected AbstractGlyphPage createGlyphPage(String path, int size) {
        // read font from input stream
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, CustomFontRenderer.class.getResourceAsStream(path)).deriveFont(60f);
        } catch(FontFormatException | IOException e) {
            e.printStackTrace();
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);

        return new CustomGlyphPage(font, size);
    }

    @Override
    protected int getScaleFactor() {
        return (int) MC.getWindow().getScaleFactor();
    }
}
