package dev.tigr.ares.fabric.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.util.render.ITextureManager;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.util.HashMap;

/**
 * @author Tigermouthbear 11/20/20
 */
public class CustomTextureManager extends HashMap<LocationIdentifier, AbstractTexture> implements ITextureManager {
    @Override
    public void bindTexture(LocationIdentifier resourceLocation) {
        AbstractTexture texture = this.get(resourceLocation);
        if(texture == null) {
            try {
                texture = new NativeImageBackedTexture(NativeImage.read(CustomTextureManager.class.getResourceAsStream(resourceLocation.getPath())));
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, texture.getGlId());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
