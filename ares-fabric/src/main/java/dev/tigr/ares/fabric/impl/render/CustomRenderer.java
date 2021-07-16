package dev.tigr.ares.fabric.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;

import static dev.tigr.ares.Wrapper.*;

/**
 * @author Tigermouthbear 11/20/20
 */
public class CustomRenderer implements IRenderer {
    private Matrix4f getMatrix() {
        return ((CustomRenderStack) RENDER_STACK).getMatrixStack().peek().getModel();
    }

    private void draw() {
        draw(false);
    }

    private void draw(boolean texture) {
        RenderSystem.depthMask(true);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        if(texture) RenderSystem.enableTexture();
        else RenderSystem.disableTexture();

        // actually draw
        Tessellator.getInstance().draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix4f = getMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) (y + height), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) (y + height), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) (y + height), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
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
        Matrix4f matrix4f = getMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture(1, 0).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) (y + height), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) (y + height), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) (y + height), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture(1, 0).next();
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
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        Matrix4f matrix4f = getMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(weight);
        bufferBuilder.vertex(matrix4f, (float) startX, (float) startY, (float) startZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) endX, (float) endY, (float) endZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        draw();
        RenderSystem.lineWidth(1f);
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

        boolean first = true;
        float firstX = 0, firstY = 0, prevX = 0, prevY = 0;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix4f = getMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(weight);
        for(int i = 0; i < points.length; i += 2) {
            if(first) {
                firstX = (float) points[i];
                firstY = (float) points[i + 1];
                first = false;
            } else bufferBuilder.vertex(matrix4f, prevX, prevY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

            prevX = (float) points[i];
            prevY = (float) points[i + 1];
            bufferBuilder.vertex(matrix4f, prevX, prevY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

            if(i >= points.length - 2) {
                bufferBuilder.vertex(matrix4f, prevX, prevY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, firstX, firstY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            }
        }
        draw();
        RenderSystem.lineWidth(1f);
    }

    @Override
    public void startScissor(double x, double y, double width, double height) {
        // calculate resolution for scissoring
        double scaleWidth = (double) MC.getWindow().getWidth() / MC.getWindow().getScaledWidth();
        double scaleHeight = (double) MC.getWindow().getHeight() / MC.getWindow().getScaledHeight();

        // enable gl scissor
        //GL20.glPushAttrib(GL20.GL_SCISSOR_BIT);
        RenderSystem.enableScissor(
                (int) (x * scaleWidth),
                (MC.getWindow().getHeight()) - (int) ((y + height) * scaleHeight),
                (int) (width * scaleWidth),
                (int) (height * scaleHeight)
        );
    }

    @Override
    public void stopScissor() {
        RenderSystem.disableScissor();
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
