package org.aresclient.ares.mixins.packet;

import org.aresclient.ares.api.packet.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.class)
public class MixinPlayerMoveC2SPacket implements PlayerMoveC2SPacket, org.aresclient.ares.api.packet.PlayerMoveC2SPacket.OnGround {
    @Mutable @Shadow @Final protected boolean onGround;

    @Mutable @Shadow @Final protected float yaw;
    @Mutable @Shadow @Final protected float pitch;

    @Mutable @Shadow @Final protected double x;
    @Mutable @Shadow @Final protected double y;
    @Mutable @Shadow @Final protected double z;

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void setOnGround(boolean value) {
        onGround = value;
    }
}
