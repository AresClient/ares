package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.network.play.client.CPacketInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketInput.class)
public interface CPacketInputAccessor {
    @Accessor("strafeSpeed")
    void setSideways(float sideways);

    @Accessor("forwardSpeed")
    void setForward(float forward);

    @Accessor("jumping")
    void setJumping(boolean jumping);

    @Accessor("sneaking")
    void setSneaking(boolean sneaking);
}
