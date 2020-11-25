package dev.tigr.ares.forge.impl.render;

import dev.tigr.ares.core.util.render.ITextureManager;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

/**
 * Used to store resource location's respective gl image bindings and other info
 *
 * @author Tigermouthbear 6/23/20
 */
public class CustomTextureManager extends HashMap<String, DynamicTexture> implements ITextureManager {
    @Override
    public void bindTexture(LocationIdentifier resourceLocation) {
        if(!containsKey(resourceLocation.getPath())) {
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(CustomTextureManager.class.getResourceAsStream(resourceLocation.getPath()));
            } catch(IOException e) {
                e.printStackTrace();
            }
            DynamicTexture dynamicTexture = new DynamicTexture(bufferedImage);
            put(resourceLocation.getPath(), dynamicTexture);
        }

        GlStateManager.bindTexture(get(resourceLocation.getPath()).getGlTextureId());
    }
}
