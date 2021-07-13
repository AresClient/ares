package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int itemUseCooldown);

    @Accessor("renderTickCounter")
    RenderTickCounter getRenderTickCounter();

    @Mutable
    @Accessor("session")
    void setSession(Session session);
}
