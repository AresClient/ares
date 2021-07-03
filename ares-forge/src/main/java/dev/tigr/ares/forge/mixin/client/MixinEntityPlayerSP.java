package dev.tigr.ares.forge.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.render.PortalChatEvent;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.movement.BlockPushEvent;
import dev.tigr.ares.forge.event.events.movement.MovePlayerEvent;
import dev.tigr.ares.forge.event.events.movement.PlayerJumpEvent;
import dev.tigr.ares.forge.event.events.player.PlayerDismountEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.MoverType;
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
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "move", at = @At(value = "HEAD"))
    public void onMotion(CallbackInfo ci) {
        Module.motion();
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void movePlayer(AbstractClientPlayer abstractClientPlayer, MoverType type, double x, double y, double z) {
        MovePlayerEvent event = Ares.EVENT_MANAGER.post(new MovePlayerEvent(type, x, y, z));
        if(!event.isCancelled()) {
            if(event.getShouldDo()) super.move(type, event.getX(), event.getY(), event.getZ());
            else super.move(type, x, y, z);
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void noPushOutOfBlocks(double var1, double var2, double var3, CallbackInfoReturnable ci) {
        if (Ares.EVENT_MANAGER.post(new BlockPushEvent(var1, var2, var3)).isCancelled()) ci.cancel();
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

    @Override
    public void jump() {
        PlayerJumpEvent event = new PlayerJumpEvent();
        Ares.EVENT_MANAGER.post(event);
        if(!event.isCancelled()) super.jump();
    }
}
