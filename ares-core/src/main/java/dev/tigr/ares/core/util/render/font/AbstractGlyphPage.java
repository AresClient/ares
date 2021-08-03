package dev.tigr.ares.core.util.render.font;

import dev.tigr.ares.core.util.render.Color;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

/**
 * @author Tigermouthbear 7/27/21
 */
public abstract class AbstractGlyphPage {
    protected final Map<Character, Glyph> characterGlyphMap = new HashMap<>();
    protected final BufferedImage bufferedImage;
    protected final int[] colorCodes = new int[32];
    protected final int width;
    protected final int height;
    protected final double glyphSize;
    protected double charHeight;

    public AbstractGlyphPage(Font font, int size) {
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

        // find scale based on size
        this.glyphSize = 256 / (double) size * 0.04f;

        // generate ascii characters
        char[] chars = new char[256];
        for(int i = 0; i < chars.length; i++) chars[i] = (char) i;

        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, true);

        // calculate character and image width and height
        double charWidth = 0;
        charHeight = 0;
        for(char c: chars) {
            Rectangle2D bounds = font.getStringBounds(Character.toString(c), fontRenderContext);

            double width = bounds.getWidth();
            double height = bounds.getHeight();

            if(width > charWidth) charWidth = width;
            if(height > charHeight) charHeight = height;
        }
        width = (int) (charWidth * 16);
        height = (int) (charHeight * 16);

        // create image and setup graphics
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();

        // setup font and colors
        graphics2D.setFont(font);
        graphics2D.setColor(new java.awt.Color(255, 255, 255, 0));
        graphics2D.fillRect(0, 0, width, height);
        graphics2D.setColor(java.awt.Color.WHITE);

        // set fractional metrics and antialiasing
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // get font metrics
        FontMetrics fontMetrics = graphics2D.getFontMetrics();

        // draw chars and create glyph objects
        for(int i = 0; i < chars.length; i++) {
            // calculate x y width and height of glyph
            int x = (int) (i % 16 * charWidth);
            int y = (int) (i / 16 * charHeight);
            Rectangle2D bounds = fontMetrics.getStringBounds(Character.toString(chars[i]), graphics2D);

            // create glyph and add to map
            Glyph glyph = new Glyph(x, y, bounds.getWidth(), bounds.getHeight());
            characterGlyphMap.put(chars[i], glyph);

            // draw glyph on glyphPagge
            graphics2D.drawString(Character.toString(chars[i]), x, y + fontMetrics.getAscent());
        }
    }

    public abstract double drawChar(char c, double x, double y, Color color);

    // draw string no shadow keep color
    public void drawString(String text, double x, double y, Color color) {
        // do regular text
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if(c == 167 && i + 1 < text.length()) {
                int colorCode = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));
                color = color(colorCodes[colorCode]);
                ++i;
            } else x += drawChar(c, x, y, color);
        }
    }

    public void drawString(String text, double x, double y, Color color, boolean shadow) {
        if(shadow) {
            double shadowX = x + 0.2;
            for(int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if(c == 167 && i + 1 < text.length()) ++i;
                else shadowX += drawChar(c, shadowX, y, Color.BLACK);
            }
        }

        drawString(text, x, y, color);
    }

    // returns height of split string
    public int drawSplitString(String text, double x, double y, Color color, double width) {
        // then text
        List<String> lines = new ArrayList<>();
        StringBuilder currLine = new StringBuilder();
        double currWidth = 0;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // dont add formatting to width
            if(c == 167 && i + 1 < text.length()) {
                currLine.append(c).append(text.charAt(i + 1));
                ++i;
            }

            // break lines at \n
            else if(c == '\n') {
                if(i == text.length() - 1) continue;
                lines.add(currLine.toString());
                currLine = new StringBuilder();
                currWidth = 0;
            }

            // write char if it fits, if it doesnt, make a new line
            else {
                if(currWidth + getCharWidth(c) > width) {
                    lines.add(currLine.toString());
                    currLine = new StringBuilder().append(c);
                    currWidth = 0;
                } else {
                    currLine.append(c);
                    currWidth += getCharWidth(c);
                }
            }
        }
        if(!currLine.toString().equals("")) lines.add(currLine.toString());

        for(String line: lines) {
            drawString(line, x, y, color);
            y += getFontHeight();
        }

        return lines.size() * getFontHeight();
    }

    public double getCharWidth(char c) {
        Glyph glyph = characterGlyphMap.get(c);
        if(glyph == null) return 0;
        return glyph.width * glyphSize;
    }

    public double getStringWidth(String text) {
        double width = 0;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == 167 && i + 1 < text.length()) i++;
            else width += getCharWidth(c);
        }

        return width;
    }

    public double getStringWidth(String text, double height) {
        return getStringWidth(text) * (height / getFontHeight());
    }

    public int getFontHeight() {
        return (int) (charHeight * glyphSize);
    }

    private Color color(int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float blue = (float) (color >> 8 & 255) / 255.0F;
        float green = (float) (color & 255) / 255.0F;
        return new Color(red, green, blue, 1);
    }

    public static class Glyph {
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        Glyph(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }
    }
}
