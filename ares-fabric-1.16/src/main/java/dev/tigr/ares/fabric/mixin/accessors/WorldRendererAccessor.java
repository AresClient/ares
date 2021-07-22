package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("entityOutlineShader")
    ShaderEffect getEntityOutlineShader();
}
