package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityAccessor {
    @Accessor("client")
    MinecraftClient client();

    @Accessor("lastSprinting")
    boolean lastSprinting();

    @Accessor("lastSprinting")
    void lastSprinting(boolean lastSprinting);

    @Accessor("lastSneaking")
    boolean lastSneaking();

    @Accessor("lastSneaking")
    void lastSneaking(boolean lastSneaking);

    @Accessor("lastOnGround")
    boolean lastOnGround();

    @Accessor("lastOnGround")
    void lastOnGround(boolean lastOnGround);

    @Accessor("lastX")
    double lastX();

    @Accessor("lastX")
    void lastX(double lastX);

    @Accessor("lastBaseY")
    double lastBaseY();

    @Accessor("lastBaseY")
    void lastBaseY(double lastBaseY);

    @Accessor("lastZ")
    double lastZ();

    @Accessor("lastZ")
    void lastZ(double lastZ);

    @Accessor("lastYaw")
    float lastYaw();

    @Accessor("lastYaw")
    void lastYaw(float lastYaw);

    @Accessor("lastPitch")
    float lastPitch();

    @Accessor("lastYaw")
    void lastPitch(float lastPitch);

    @Accessor("autoJumpEnabled")
    boolean autoJumpEnabled();

    @Accessor("autoJumpEnabled")
    void autoJumpEnabled(boolean autoJumpEnabled);

    @Accessor("ticksSinceLastPositionPacketSent")
    int ticksSinceLastPositionPacketSent();

    @Accessor("ticksSinceLastPositionPacketSent")
    void ticksSinceLastPositionPacketSent(int ticksSinceLastPositionPacketSent);
}
