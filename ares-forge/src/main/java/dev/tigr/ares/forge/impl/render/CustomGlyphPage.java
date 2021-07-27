package dev.tigr.ares.forge.impl.render;

import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.font.AbstractGlyphPage;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;

import static dev.tigr.ares.Wrapper.MC;
import static net.minecraft.client.renderer.GlStateManager.glTexParameteri;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;

/**
 * @author Tigermouthbear 7/27/21
 */
public class CustomGlyphPage extends AbstractGlyphPage {
    private final DynamicTexture texture;

    public CustomGlyphPage(Font font, int size) {
        super(font, size);

        // create glyph page dynamic texture
        // https://github.com/kami-blue/client/blob/005d5ab2d64eda15cfdf88813db221820a3ee4a1/src/main/java/me/zeroeightsix/kami/util/graphics/font/FontGlyphs.kt#L177
        texture = new DynamicTexture(bufferedImage);
        try {
            texture.loadTexture(MC.getResourceManager());
        } catch(IOException e) {
            e.printStackTrace();
        }
        int textureId = texture.getGlTextureId();

        // Tells Gl that our texture isn't a repeating texture (edges are not connecting to each others)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Setup texture filters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);

        // Setup mipmap parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 3);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 3);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0f);
        GlStateManager.bindTexture(textureId);

        for(int mipmapLevel = 0; mipmapLevel < 4; mipmapLevel++)
            glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_ALPHA, bufferedImage.getWidth() >> mipmapLevel, bufferedImage.getHeight() >> mipmapLevel, 0, GL_ALPHA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, 1);
        TextureUtil.uploadTextureImageSub(textureId, bufferedImage, 0, 0, true, true);
    }

    @Override
    public double drawChar(char c, double x, double y, Color color) {
        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.glLineWidth(1);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.bindTexture(texture.getGlTextureId());
        glBegin(GL_TRIANGLES);
        {
            glTexCoord2d(texX + texWidth, texY);
            glVertex2d(x + scaledWidth, y);

            glTexCoord2d(texX, texY);
            glVertex2d(x, y);

            glTexCoord2d(texX, texY + texHeight);
            glVertex2d(x, y + scaledHeight);

            glTexCoord2d(texX, texY + texHeight);
            glVertex2d(x, y + scaledHeight);

            glTexCoord2d(texX + texWidth, texY + texHeight);
            glVertex2d(x + scaledWidth, y + scaledHeight);

            glTexCoord2d(texX + texWidth, texY);
            glVertex2d(x + scaledWidth, y);
        }
        glEnd();

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();

        return glyph.getWidth() * glyphSize;
    }
}
