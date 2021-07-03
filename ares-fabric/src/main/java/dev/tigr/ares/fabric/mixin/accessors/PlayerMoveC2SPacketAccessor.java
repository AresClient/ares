package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Accessor("yaw")
    void setYaw(float yaw);

    @Accessor("pitch")
    void setPitch(float yaw);

    @Accessor("y")
    void setY(double y);

    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
