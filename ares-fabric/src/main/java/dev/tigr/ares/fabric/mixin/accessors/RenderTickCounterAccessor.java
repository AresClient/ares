package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderTickCounter.class)
public interface RenderTickCounterAccessor {
    @Mutable @Accessor("tickTime")
    void setTickTime(float tickTime);

    @Accessor("tickTime")
    float getTickTime();
}
