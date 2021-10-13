package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityPlayerSP.class)
public interface EntityPlayerSPAccessor {
    @Accessor("serverSprintState")
    boolean serverSprintState();

    @Accessor("serverSprintState")
    void serverSprintState(boolean serverSprintState);

    @Accessor("serverSneakState")
    boolean serverSneakState();

    @Accessor("serverSneakState")
    void serverSneakState(boolean serverSneakState);

    @Accessor("lastReportedPosX")
    double lastReportedPosX();

    @Accessor("lastReportedPosX")
    void lastReportedPosX(double lastReportedPosX);

    @Accessor("lastReportedPosY")
    double lastReportedPosY();

    @Accessor("lastReportedPosY")
    void lastReportedPosY(double lastReportedPosY);

    @Accessor("lastReportedPosZ")
    double lastReportedPosZ();

    @Accessor("lastReportedPosZ")
    void lastReportedPosZ(double lastReportedPosZ);

    @Accessor("lastReportedYaw")
    float lastReportedYaw();

    @Accessor("lastReportedYaw")
    void lastReportedYaw(float lastReportedYaw);

    @Accessor("lastReportedPitch")
    float lastReportedPitch();

    @Accessor("lastReportedPitch")
    void lastReportedPitch(float lastReportedPitch);

    @Accessor("positionUpdateTicks")
    int positionUpdateTicks();

    @Accessor("positionUpdateTicks")
    void positionUpdateTicks(int positionUpdateTicks);

    @Accessor("prevOnGround")
    boolean prevOnGround();

    @Accessor("prevOnGround")
    void prevOnGround(boolean prevOnGround);

    @Accessor("autoJumpEnabled")
    boolean autoJumpEnabled();

    @Accessor("autoJumpEnabled")
    void autoJumpEnabled(boolean autoJumpEnabled);

    @Invoker("isCurrentViewEntity")
    boolean isCurrentViewEntity();
}
