package dev.tigr.ares.forge.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.movement.MovePlayerEvent;
import dev.tigr.ares.core.event.movement.SendMovementPacketsEvent;
import dev.tigr.ares.core.event.movement.SetPlayerSprintEvent;
import dev.tigr.ares.core.event.render.PortalChatEvent;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.feature.module.modules.movement.AutoSprint;
import dev.tigr.ares.core.feature.module.modules.player.Freecam;
import dev.tigr.ares.core.util.math.doubles.V3D;
import dev.tigr.ares.forge.event.events.movement.BlockPushEvent;
import dev.tigr.ares.forge.event.events.movement.WalkOffLedgeEvent;
import dev.tigr.ares.forge.event.events.player.PlayerDismountEvent;
import dev.tigr.ares.forge.mixin.accessor.EntityPlayerSPAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Tigermouthbear
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer implements Wrapper {
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "move", at = @At(value = "HEAD"))
    public void onMotion(CallbackInfo ci) {
        Module.motion();
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void onMovePlayer(MoverType movementType, double x, double y, double z, CallbackInfo ci) {
        MovePlayerEvent moveEvent = Ares.EVENT_MANAGER.post(new MovePlayerEvent(movementType.name(), x, y, z));
        WalkOffLedgeEvent safeWalkEvent = Ares.EVENT_MANAGER.post(new WalkOffLedgeEvent());

        if(moveEvent.isCancelled() || safeWalkEvent.isCancelled())
            ci.cancel();

        if(moveEvent.isCancelled()) {
            if(safeWalkEvent.isCancelled()) {
                V3D move = clipAtLedge(moveEvent.getX(), moveEvent.getY(), moveEvent.getZ());
                super.move(movementType, move.getX(), move.getY(), move.getZ());
            }

            else super.move(movementType, moveEvent.getX(), moveEvent.getY(), moveEvent.getZ());
        }

        else if(safeWalkEvent.isCancelled()) {
            V3D move = clipAtLedge(moveEvent.getX(), moveEvent.getY(), moveEvent.getZ());
            super.move(movementType, move.getX(), move.getY(), move.getZ());
        }
    }

    private V3D clipAtLedge(double x, double y, double z) {
        double x1 = x, y1 = y, z1 = z;
        if(MC.player.onGround && !MC.player.noClip) {
            double d5;
            for(d5 = 0.05D; x1 != 0.0D && MC.world.getCollisionBoxes(MC.player, MC.player.getEntityBoundingBox().offset(x1, -MC.player.stepHeight, 0.0D)).isEmpty();) {
                if(x1 < d5 && x1 >= -d5) x1 = 0.0D;
                else if(x1 > 0.0D) x1 -= d5;
                else x1 += d5;
            }
            for(; z1 != 0.0D && MC.world.getCollisionBoxes(MC.player, MC.player.getEntityBoundingBox().offset(0.0D, -MC.player.stepHeight, z1)).isEmpty();) {
                if(z1 < d5 && z1 >= -d5) z1 = 0.0D;
                else if(z1 > 0.0D) z1 -= d5;
                else z1 += d5;
            }
            for(; x1 != 0.0D && z1 != 0.0D && MC.world.getCollisionBoxes(MC.player, MC.player.getEntityBoundingBox().offset(x1, -MC.player.stepHeight, z1)).isEmpty();) {
                if(x1 < d5 && x1 >= -d5) x1 = 0.0D;
                else if(x1 > 0.0D) x1 -= d5;
                else x1 += d5;

                if(z1 < d5 && z1 >= -d5) z1 = 0.0D;
                else if(z1 > 0.0D) z1 -= d5;
                else z1 += d5;
            }
        }

        return new V3D(x1, y1, z1);
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void onSetSprint(boolean sprinting, CallbackInfo ci) {
        SetPlayerSprintEvent event = Ares.EVENT_MANAGER.post(new SetPlayerSprintEvent(sprinting));
        if(event.isCancelled()) {
            super.setSprinting(event.isSprinting());
            ci.cancel();
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void noPushOutOfBlocks(double d2, double f, double blockpos, CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new BlockPushEvent()).isCancelled()) cir.cancel();
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP) {
        if(Ares.EVENT_MANAGER.post(new PortalChatEvent()).isCancelled()) return;
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        if(Ares.EVENT_MANAGER.post(new PortalChatEvent()).isCancelled()) return;
    }

    @Inject(method = "dismountRidingEntity", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onDismountStart(CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new PlayerDismountEvent.Start(Minecraft.getMinecraft().player.getRidingEntity()));
    }

    @Inject(method = "dismountRidingEntity", at = @At("RETURN"))
    public void onDismountEnd(CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new PlayerDismountEvent.End());
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onSendMovementPackets(CallbackInfo ci) {
        SendMovementPacketsEvent event = new SendMovementPacketsEvent.Pre(new V3D(getPositionVector().x, getPositionVector().y, getPositionVector().z), onGround);
        if(Ares.EVENT_MANAGER.post(event).isCancelled()) {
            if(!event.isModifying()) {
                ci.cancel();
                return;
            }
            ci.cancel();

            //Modified packets
            boolean flag = AutoSprint.INSTANCE.getEnabled() || MC.player.isSprinting();

            if(flag != ((EntityPlayerSPAccessor) MC.player).serverSprintState()) {
                if(flag) MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_SPRINTING));
                else MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.STOP_SPRINTING));

                ((EntityPlayerSPAccessor) MC.player).serverSprintState(flag);
            }

            boolean flag1 = MC.player.isSneaking();

            if(flag1 != ((EntityPlayerSPAccessor) MC.player).serverSneakState()) {
                if(flag1) MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_SNEAKING));
                else MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.STOP_SNEAKING));

                ((EntityPlayerSPAccessor) MC.player).serverSneakState(flag1);
            }

            if(((EntityPlayerSPAccessor) MC.player).isCurrentViewEntity()) {
                double d0 = MC.player.posX - ((EntityPlayerSPAccessor) MC.player).lastReportedPosX();
                double d1 = event.getPos().y - ((EntityPlayerSPAccessor) MC.player).lastReportedPosY();
                double d2 = MC.player.posZ - ((EntityPlayerSPAccessor) MC.player).lastReportedPosZ();
                double d3 = event.getYaw() - ((EntityPlayerSPAccessor) MC.player).lastReportedYaw();
                double d4 = event.getPitch() - ((EntityPlayerSPAccessor) MC.player).lastReportedPitch();
                ((EntityPlayerSPAccessor) MC.player).positionUpdateTicks(((EntityPlayerSPAccessor) MC.player).positionUpdateTicks() +1);
                boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || ((EntityPlayerSPAccessor) MC.player).positionUpdateTicks() >= 20;
                boolean flag3 = d3 != 0.0D || d4 != 0.0D;

                if(Freecam.INSTANCE.getEnabled()) flag2 = false;

                if(MC.player.isRiding()) {
                    if(!Freecam.INSTANCE.getEnabled()) MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.motionX, -999.0D, MC.player.motionZ, event.getYaw(), event.getPitch(), MC.player.onGround));
                    flag2 = false;
                }

                else if(flag2 && flag3) MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.posX, event.getPos().y, MC.player.posZ, event.getYaw(), event.getPitch(), MC.player.onGround));

                else if(flag2) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, event.getPos().y, MC.player.posZ, MC.player.onGround));

                else if(flag3) {
                    if(Freecam.INSTANCE.getEnabled() && !event.isModifying())
                        MC.player.connection.sendPacket(new CPacketPlayer.Rotation(MC.world.getEntityByID(Freecam.INSTANCE.clone).rotationYaw, MC.world.getEntityByID(Freecam.INSTANCE.clone).rotationPitch, MC.player.onGround));

                    else MC.player.connection.sendPacket(new CPacketPlayer.Rotation(event.getYaw(), event.getPitch(), MC.player.onGround));
                }

                else if(((EntityPlayerSPAccessor) MC.player).prevOnGround() != MC.player.onGround) MC.player.connection.sendPacket(new CPacketPlayer(MC.player.onGround));

                if(flag2) {
                    ((EntityPlayerSPAccessor) MC.player).lastReportedPosX(MC.player.posX);
                    ((EntityPlayerSPAccessor) MC.player).lastReportedPosY(event.getPos().y);
                    ((EntityPlayerSPAccessor) MC.player).lastReportedPosZ(MC.player.posZ);
                    ((EntityPlayerSPAccessor) MC.player).positionUpdateTicks(0);
                }

                if(flag3) {
                    ((EntityPlayerSPAccessor) MC.player).lastReportedYaw(event.getYaw());
                    ((EntityPlayerSPAccessor) MC.player).lastReportedPitch(event.getPitch());
                }

                ((EntityPlayerSPAccessor) MC.player).prevOnGround(MC.player.onGround);
                ((EntityPlayerSPAccessor) MC.player).autoJumpEnabled(MC.gameSettings.autoJump);
            }

            Ares.EVENT_MANAGER.post(new SendMovementPacketsEvent.Post(event.getYaw(), event.getPitch()));
        }
    }
}
