package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.render.MapRenderer.MapTexture")
public interface MapTextureAccessor {
    @Accessor("renderLayer")
    RenderLayer getRenderLayer();
}
