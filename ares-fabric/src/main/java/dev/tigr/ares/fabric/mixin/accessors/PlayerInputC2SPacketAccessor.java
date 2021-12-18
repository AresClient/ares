package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInputC2SPacket.class)
public interface PlayerInputC2SPacketAccessor {
    @Mutable @Accessor("sideways")
    void setSideways(float sideways);

    @Mutable @Accessor("forward")
    void setForward(float forward);

    @Mutable @Accessor("jumping")
    void setJumping(boolean jumping);

    @Mutable @Accessor("sneaking")
    void setSneaking(boolean sneaking);
}
