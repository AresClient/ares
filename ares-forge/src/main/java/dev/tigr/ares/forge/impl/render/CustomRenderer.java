package dev.tigr.ares.forge.impl.render;

import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import static dev.tigr.ares.Wrapper.*;

/**
 * @author Tigermouthbear 11/19/20
 */
public class CustomRenderer implements IRenderer {
    private void draw() {
        draw(false);
    }

    private void draw(boolean texture) {
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();

        GlStateManager.disableCull();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        if(texture) GlStateManager.enableTexture2D();
        else GlStateManager.disableTexture2D();

        // actually draw
        Tessellator.getInstance().draw();

        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Draws a rectangle on the GUI with specified color
     *
     * @param x      x position of the rectangle
     * @param y      y position of the rectangle
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param color  color of the rectangle
     */
    @Override
    public void drawRect(double x, double y, double width, double height, Color color) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(x + width, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x, y + height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x, y + height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x + width, y + height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x + width, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        draw();
    }

    /**
     * Draws an Image using an {@link LocationIdentifier}
     *
     * @param x      x position of the image
     * @param y      y position of the image
     * @param width  width of the image
     * @param height height of the image
     * @param identifier  {@link LocationIdentifier} of the image to render
     */
    @Override
    public void drawImage(double x, double y, double width, double height, LocationIdentifier identifier) {
        drawImage(x, y, width, height, identifier, Color.WHITE);
    }

    @Override
    public void drawImage(double x, double y, double width, double height, LocationIdentifier identifier, Color color) {
        // bind texture
        TEXTURE_MANAGER.bindTexture(identifier);

        // draw it
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferBuilder.pos(x + width, y, 0).tex(1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x, y, 0).tex(0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x, y + height, 0).tex(0, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x, y + height, 0).tex(0, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x + width, y + height, 0).tex(1, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(x + width, y, 0).tex(1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        draw(true);
    }

    /**
     * Draws a line with the given start and end
     *
     * @param startX x position of the first point
     * @param startY y position of the first point
     * @param endX   x position of the second point
     * @param endY   y position of the second point
     * @param weight weight of the line
     * @param color  color of the line
     */
    @Override
    public void drawLine(double startX, double startY, double endX, double endY, int weight, Color color) {
        drawLine(startX, startY, 0, endX, endY, 0, weight, color);
    }

    /**
     * Draws a line with the given start and end
     *
     * @param startX x position of the first point
     * @param startY y position of the first point
     * @param startZ z position of the first point
     * @param endX   x position of the second point
     * @param endY   y position of the second point
     * @param endZ   z position of the second point
     * @param weight weight of the line
     * @param color  color of the line
     */
    public void drawLine(double startX, double startY, double startZ, double endX, double endY, double endZ, int weight, Color color) {
        GlStateManager.glLineWidth(weight);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(startX, startY, startZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        draw();
    }

    /**
     * Draws a loop of lines using points
     *
     * @param weight weight of the line
     * @param color  color of the line
     * @param points array of points, a point being two double values (the first is x the second is y)
     */
    @Override
    public void drawLineLoop(int weight, Color color, double... points) {
        if(points.length % 2 != 0) return;

        GlStateManager.glLineWidth(weight);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i < points.length; i += 2)
            bufferBuilder.pos(points[i], points[i + 1], 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        draw();
    }

    @Override
    public void startScissor(double x, double y, double width, double height) {
        ScaledResolution resolution = new ScaledResolution(MC);
        double scaleWidth = (double) MC.displayWidth / resolution.getScaledWidth_double();
        double scaleHeight = (double) MC.displayHeight / resolution.getScaledHeight_double();
        GL11.glScissor(
                (int) (x * scaleWidth),
                (MC.displayHeight) - (int) ((y + height) * scaleHeight),
                (int) (width * scaleWidth),
                (int) (height * scaleHeight)
        );
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void stopScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void drawTooltip(String text, int mouseX, int mouseY, Color color) {
        int buffer = 2;
        int x = mouseX + 5;
        int y = mouseY + 5;
        double width = FONT_RENDERER.getStringWidth(text) + buffer * 2;
        int height = FONT_RENDERER.getFontHeight() + buffer * 2;

        drawRect(x, y, width, height, Color.BLACK);

        FONT_RENDERER.drawString(text, x + buffer, y + buffer, Color.WHITE);

        drawLineLoop(1, color,
                x, y,
                x + width, y,
                x + width, y + height,
                x, y + height
        );
    }
}
