package org.aresclient.ares.mixins.packet;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(PlayerMoveC2SPacket.LookAndOnGround.class)
public class MixinPlayerMoveC2SPacketLookAndOnGround extends MixinPlayerMoveC2SPacket implements org.aresclient.ares.api.packet.PlayerMoveC2SPacket.Rotation {
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
}