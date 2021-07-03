package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {
    @Accessor("playerVelocityX")
    void setPlayerVelocityX(float velocityX);

    @Accessor("playerVelocityY")
    void setPlayerVelocityY(float velocityY);

    @Accessor("playerVelocityZ")
    void setPlayerVelocityZ(float velocityZ);
}
