package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPositionLookS2CPacket.class)
public interface PlayerPositionLookS2CPacketAccessor {
    @Mutable @Accessor("yaw")
    void setYaw(float yaw);

    @Mutable @Accessor("pitch")
    void setPitch(float yaw);
}
