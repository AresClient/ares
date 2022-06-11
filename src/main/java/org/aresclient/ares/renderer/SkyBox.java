package org.aresclient.ares.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkyBox {
    private static final List<SkyBox> SKY_BOXES = new ArrayList<>();
    private static final Shader SHADER = Shader.fromResources("/assets/ares/shaders/vert/skybox.vert", "/assets/ares/shaders/frag/skybox.frag");
    private static final Buffer BUFFER = Buffer
            .beginStatic(SHADER, VertexFormat.POSITION, 8, 36)
            .vertices(
                    -1, 1, -1,
                    -1, -1, -1,
                    1, 1, -1,
                    1, -1, -1,
                    -1, 1, 1,
                    1, 1, 1,
                    -1, -1, 1,
                    1, -1, 1
            )
            .indices(
                    // front
                    0, 1, 2,
                    2, 1, 3,
                    // right
                    2, 3, 5,
                    5, 3, 7,
                    // back
                    5, 7, 4,
                    4, 7, 6,
                    // left
                    4, 6, 0,
                    0, 6, 1,
                    // top
                    4, 0, 5,
                    5, 0, 2,
                    // bottom
                    1, 6, 3,
                    3, 6, 7
            )
            .end();

    private final int texture = GL11.glGenTextures();

    public SkyBox(String path) {
        GL11.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);

        for(int i = 0; i < 6; i++) {
            BufferedImage image;
            try {
                image = ImageIO.read(SkyBox.class.getResourceAsStream(path + "_" + i + ".jpg"));
            } catch(IOException e) {
                throw new RuntimeException(e);
            }

            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Texture.readImage(image));
        }

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);

        SKY_BOXES.add(this);
    }

    public void render(MatrixStack matrixStack) {
        GL11.glDepthMask(false);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);

        BUFFER.draw(matrixStack);

        GL11.glDepthMask(true);
    }

    public void delete() {
        GL11.glDeleteTextures(texture);
    }

    public static void clear() {
        for(SkyBox skyBox: SKY_BOXES) {
            GL11.glDeleteTextures(skyBox.texture);
        }
        SKY_BOXES.clear();
    }
}
