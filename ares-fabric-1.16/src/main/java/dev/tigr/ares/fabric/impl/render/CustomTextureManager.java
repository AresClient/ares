package dev.tigr.ares.fabric.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.util.render.ITextureManager;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author Tigermouthbear 11/20/20
 */
public class CustomTextureManager extends HashMap<String, AbstractTexture> implements ITextureManager {
    @Override
    public void bindTexture(LocationIdentifier resourceLocation) {
        if(!containsKey(resourceLocation.getPath())) {
            BufferedImage bufferedImage;
            AbstractTexture texture = null;
            try {
                bufferedImage = ImageIO.read(CustomTextureManager.class.getResourceAsStream(resourceLocation.getPath()));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                byte[] bytes = baos.toByteArray();

                ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
                data.flip();

                texture = new NativeImageBackedTexture(NativeImage.read(data));
            } catch(IOException e) {
                e.printStackTrace();
            }
            assert texture != null;
            put(resourceLocation.getPath(), texture);
        }

        RenderSystem.bindTexture(get(resourceLocation.getPath()).getGlId());
    }
}
