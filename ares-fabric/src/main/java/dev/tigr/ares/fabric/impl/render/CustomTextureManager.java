package dev.tigr.ares.fabric.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.util.render.ITextureManager;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

import java.util.HashMap;

/**
 * @author Tigermouthbear 11/20/20
 */
public class CustomTextureManager extends HashMap<LocationIdentifier, Identifier> implements ITextureManager {
    @Override
    public void bindTexture(LocationIdentifier resourceLocation) {
        Identifier identifier = this.get(resourceLocation);
        if(identifier == null) {
            identifier = new Identifier(resourceLocation.getPath());
            put(resourceLocation, identifier);
        }

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, identifier);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
