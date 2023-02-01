package org.aresclient.ares.mixins.packet;

import org.aresclient.ares.api.packet.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full.class)
public class MixinPlayerMoveC2SPacketFull extends MixinPlayerMoveC2SPacket implements PlayerMoveC2SPacket.PositionRotation {
    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public void setYaw(float value) {
        yaw = value;
    }

    @Override
    public void setPitch(float value) {
        pitch = value;
    }

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
