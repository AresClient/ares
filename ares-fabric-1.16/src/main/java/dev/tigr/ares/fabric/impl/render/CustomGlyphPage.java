package dev.tigr.ares.fabric.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.font.AbstractGlyphPage;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static dev.tigr.ares.CoreWrapper.RENDER_STACK;

/**
 * @author Tigermouthbear 7/27/21
 */
public class CustomGlyphPage extends AbstractGlyphPage {
    private final AbstractTexture texture;

    public CustomGlyphPage(Font font, int size) {
        super(font, size);

        // create texture
        AbstractTexture texture1;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();

            texture1 = new NativeImageBackedTexture(NativeImage.read(data));
        } catch (Exception e) {
            texture1 = null;
            e.printStackTrace();
        }
        texture = texture1;
    }

    @Override
    public double drawChar(char c, double x, double y, Color color) {
        Glyph glyph = characterGlyphMap.get(c);
        if(glyph == null) return 0;

        // calculate texture coords
        double texX = glyph.getX() / (double) width;
        double texY = glyph.getY() / (double) height;
        double texWidth = glyph.getWidth() / (double) width;
        double texHeight = glyph.getHeight() / (double) height;

        // calculate scaled width and height
        double scaledWidth = glyph.getWidth() * glyphSize;
        double scaledHeight = glyph.getHeight() * glyphSize;

        if(texture != null) {
            RenderSystem.bindTexture(texture.getGlId());

            Matrix4f matrices = ((CustomRenderStack) RENDER_STACK).getMatrixStack().peek().getModel();
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE);
            bufferBuilder.vertex(matrices, (float) (x + scaledWidth), (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture((float) (texX + texWidth), (float) texY).next();
            bufferBuilder.vertex(matrices, (float) x, (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture((float) texX, (float) texY).next();
            bufferBuilder.vertex(matrices, (float) x, (float) (y + scaledHeight), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture((float) texX, (float) (texY + texHeight)).next();
            bufferBuilder.vertex(matrices, (float) x, (float) (y + scaledHeight), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture((float) texX, (float) (texY + texHeight)).next();
            bufferBuilder.vertex(matrices, (float) (x + scaledWidth), (float) (y + scaledHeight), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture((float) (texX + texWidth), (float) (texY + texHeight)).next();
            bufferBuilder.vertex(matrices, (float) (x + scaledWidth), (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).texture((float) (texX + texWidth), (float) texY).next();
            bufferBuilder.end();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.disableCull();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(1);
            RenderSystem.shadeModel(GL11.GL_SMOOTH);

            BufferRenderer.draw(bufferBuilder);

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.enableTexture();
        }

        return glyph.getWidth() * glyphSize;
    }
}
