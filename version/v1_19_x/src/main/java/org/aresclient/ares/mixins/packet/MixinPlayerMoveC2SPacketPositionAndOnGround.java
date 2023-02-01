package org.aresclient.ares.mixins.packet;

import org.aresclient.ares.api.packet.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround.class)
public class MixinPlayerMoveC2SPacketPositionAndOnGround extends MixinPlayerMoveC2SPacket implements PlayerMoveC2SPacket.Position {
    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setX(double value) {
        z = value;
    }

    @Override
    public void setY(double value) {
        y = value;
    }

    @Override
    public void setZ(double value) {
        z = value;
    }
}
