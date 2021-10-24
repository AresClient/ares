package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
    @Accessor("thirdPersonDistancePrev")
    float getThirdPersonDistancePrev();

    @Accessor("cloudFog")
    boolean getCloudFog();

    @Accessor("cloudFog")
    void setCloudFog(boolean cloudFog);
}
