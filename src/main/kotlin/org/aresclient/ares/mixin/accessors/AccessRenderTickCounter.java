package org.aresclient.ares.mixin.accessors;

import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderTickCounter.Dynamic.class)
public interface AccessRenderTickCounter {
	@Accessor("tickTime") @Mutable void setTickTime(float value);
}
