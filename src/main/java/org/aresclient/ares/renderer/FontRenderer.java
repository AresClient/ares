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
    private static final Buffer BUFFER = Buffer.createDynamic(Shader.POSITION_TEXTURE_COLOR, VertexFormat.POSITION_UV_COLOR);

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

            if(width > charWidth) charWidth = width + 2;
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

    public double drawChar(MatrixStack matrixStack, char c, float x, float y, float r, float g, float b, float a) {
        double w = drawChar(BUFFER, c, x, y, r, g, b, a);
        texture.bind();
        BUFFER.draw(matrixStack);
        BUFFER.reset();
        return w;
    }

    public double drawChar(Buffer buffer, char c, float x, float y, float r, float g, float b, float a) {
        Glyph glyph = glyphMap.get(c);
        if(glyph == null) return 0;

        float w = glyph.width / 2f;
        float h = glyph.height / 2f;
        float tx = glyph.x / width;
        float ty = glyph.y / height;
        float tw = glyph.width / width;
        float th = glyph.height / height;

        buffer.indicesOffset(
                0, 1, 2,
                1, 2, 3
        );
        buffer.vertices(
                x + w, y + h, 0, tx + tw, ty + th, r, g, b, a,
                x + w, y, 0, tx + tw, ty, r, g, b, a,
                x, y + h, 0, tx, ty + th, r, g, b, a,
                x, y, 0, tx, ty, r, g, b, a
        );

        return w;
    }

    public void drawString(MatrixStack matrixStack, String text, float x, float y, float r, float g, float b, float a) {
        drawString(BUFFER, text, x, y, r, g, b, a);
        texture.bind();
        BUFFER.draw(matrixStack);
        BUFFER.reset();
    }

    public void drawString(Buffer buffer, String text, float x, float y, float r, float g, float b, float a) {
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == 167 && i + 1 < text.length()) {
                int colorCode = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));
                int color = colorCodes[colorCode];

                r = (float) (color >> 16 & 255) / 255.0F;
                g = (float) (color >> 8 & 255) / 255.0F;
                b = (float) (color & 255) / 255.0F;

                ++i;
            } else x += drawChar(buffer, c, x, y, r, g, b, a);
        }
    }

    public float getCharHeight() {
        return charHeight;
    }

    public float getCharWidth(char c) {
        Glyph glyph = glyphMap.get(c);
        if(glyph == null) return 0;
        return glyph.width / 2f;
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

    public void bindTexture() {
        texture.bind();
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
