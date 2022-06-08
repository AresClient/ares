package org.aresclient.ares.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Texture {
    private static final List<Texture> TEXTURES = new ArrayList<>();

    private final int id = GL11.glGenTextures();

    public Texture(InputStream inputStream) {
        try {
            generate(ImageIO.read(inputStream), id);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        TEXTURES.add(this);
    }

    public Texture(File file) {
        try {
            generate(ImageIO.read(file), id);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        TEXTURES.add(this);
    }

    public Texture(BufferedImage image) {
        generate(image, id);
        TEXTURES.add(this);
    }

    public static ByteBuffer readImage(BufferedImage image) {
        // read image data
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        // allocate buffer to create color data
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        return buffer;
    }

    private static void generate(BufferedImage image, int id) {
        // generate texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, readImage(image));
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void bind() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public void delete() {
        GL11.glDeleteTextures(id);
    }

    public static void clear() {
        for(Texture texture: TEXTURES) {
            GL11.glDeleteTextures(texture.id);
        }
        TEXTURES.clear();
    }
}
