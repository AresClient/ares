package org.aresclient.ares.renderer;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FontRenderer {
    private static final Shader SHADER = Shader.fromResources("/assets/ares/shaders/vert/font.vert", "/assets/ares/shaders/frag/font.frag");
    private static final Uniform.F4 DIMENSIONS = SHADER.uniformF4("dimensions");
    private static final Uniform.F3 COLOR = SHADER.uniformF3("color").set(1, 1, 1);
    private static final Buffer BUFFER = Buffer
            .beginStatic(SHADER, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                    1, 1, 0,    1, 1,
                    1, 0, 0,   1, 0,
                    0, 1, 0,   0, 1,
                    0, 0, 0,  0, 0
            )
            .indices(
                    0, 1, 2,
                    1, 2, 3
            )
            .uniform(DIMENSIONS)
            .uniform(COLOR)
            .end();

    private final Map<Character, Glyph> glyphMap = new HashMap<>();
    private final int[] colorCodes = new int[32];
    private final Texture texture;
    private final int width, height;
    private final float charHeight;

    public FontRenderer(Font font, float size, int style) {
        this(font.deriveFont(style), size);
    }

    public FontRenderer(Font font, float size) {
        // generate color codes
        for(int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if(i == 6) k += 85;

            if(i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            colorCodes[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }

        // generate ascii characters
        char[] chars = new char[256];
        for(int i = 0; i < chars.length; i++) chars[i] = (char) i;

        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, true);
        font = font.deriveFont(size * 2);

        // calculate max character and image width and height
        float charWidth = 0;
        float tempCharHeight = 0;
        for(char c: chars) {
            Rectangle2D bounds = font.getStringBounds(Character.toString(c), fontRenderContext);
            float width = (float) bounds.getWidth();
            float height = (float) bounds.getHeight();

            if(width > charWidth) charWidth = width;
            if(height > tempCharHeight) tempCharHeight = height;
        }
        charHeight = tempCharHeight;
        width = (int) (charWidth * 16);
        height = (int) (charHeight * 16);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = (Graphics2D) image.getGraphics();

        graphics2D.setFont(font);
        graphics2D.setColor(new Color(0, 0, 0, 0));
        graphics2D.fillRect(0, 0, width, height);
        graphics2D.setColor(Color.WHITE);

        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw chars and create glyph objects
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        for(int i = 0; i < chars.length; i++) {
            // calculate x y width and height of glyph
            int x = (int) (i % 16 * charWidth);
            int y = (int) (i / 16 * charHeight);
            Rectangle2D bounds = fontMetrics.getStringBounds(Character.toString(chars[i]), graphics2D);

            // create glyph and add to map
            Glyph glyph = new Glyph(x, y, (float) bounds.getWidth(), (float) bounds.getHeight());
            glyphMap.put(chars[i], glyph);

            // draw glyph on glyphPage
            graphics2D.drawString(Character.toString(chars[i]), x, y + fontMetrics.getAscent());
        }

        texture = new Texture(image);
    }

    public double drawChar(MatrixStack matrixStack, char c, float x, float y, float r, float g, float b) {
        Glyph glyph = glyphMap.get(c);
        if(glyph == null) return 0;

        matrixStack.push();
        matrixStack.model().translate(x, y, 0).scale(glyph.width, glyph.height, 1).scaleLocal(0.5f, 0.5f, 1);

        DIMENSIONS.set(glyph.x / width, glyph.y / height, glyph.width / width, glyph.height / height);
        COLOR.set(r, g, b);

        texture.bind();
        BUFFER.draw(matrixStack);

        matrixStack.pop();

        return glyph.width;
    }

    public void drawString(MatrixStack matrixStack, String text, float x, float y, float r, float g, float b) {
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == 167 && i + 1 < text.length()) {
                int colorCode = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));
                int color = colorCodes[colorCode];

                r = (float) (color >> 16 & 255) / 255.0F;
                g = (float) (color >> 8 & 255) / 255.0F;
                b = (float) (color & 255) / 255.0F;

                ++i;
            } else x += drawChar(matrixStack, c, x, y, r, g, b);
        }
    }

    public float getCharHeight() {
        return charHeight;
    }

    public float getCharWidth(char c) {
        Glyph glyph = glyphMap.get(c);
        if(glyph == null) return 0;
        return glyph.width;
    }

    public float getStringWidth(String text) {
        float width = 0;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == 167 && i + 1 < text.length()) i++;
            else width += getCharWidth(c);
        }

        return width;
    }

    public void delete() {
        texture.delete();
    }

    private static class Glyph {
        private final float x, y, width, height;

        private Glyph(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
