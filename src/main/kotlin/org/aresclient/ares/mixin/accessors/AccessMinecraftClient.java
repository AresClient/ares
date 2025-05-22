package org.aresclient.ares.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface AccessMinecraftClient {
	@Accessor("renderTickCounter") RenderTickCounter.Dynamic getRenderTickCounter();

	@Accessor("itemUseCooldown")
	void setItemUseCooldown(int value);
}
