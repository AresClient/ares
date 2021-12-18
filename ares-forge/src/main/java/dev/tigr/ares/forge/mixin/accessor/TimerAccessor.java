package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface TimerAccessor {
    @Accessor("tickLength")
    float getTickLength();

    @Accessor("tickLength")
    void setTickLength(float tickLength);
}
