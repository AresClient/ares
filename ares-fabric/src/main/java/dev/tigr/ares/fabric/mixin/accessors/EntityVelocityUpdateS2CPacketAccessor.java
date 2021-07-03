package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {
    @Accessor("velocityX")
    void setVelocityX(int velocityX);

    @Accessor("velocityY")
    void setVelocityY(int velocityY);

    @Accessor("velocityZ")
    void setVelocityZ(int velocityZ);
}
