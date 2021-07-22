package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {
    @Mutable @Accessor("playerVelocityX")
    void setPlayerVelocityX(float velocityX);

    @Mutable @Accessor("playerVelocityY")
    void setPlayerVelocityY(float velocityY);

    @Mutable @Accessor("playerVelocityZ")
    void setPlayerVelocityZ(float velocityZ);
}
