package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.player.InteractEvent;
import dev.tigr.ares.forge.gui.AresChatGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Final public Profiler profiler;
    private final Minecraft MC = ((Minecraft) (Object) this);

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    private boolean isHandActive(EntityPlayerSP playerSP) {
        return Ares.EVENT_MANAGER.post(new InteractEvent(playerSP.isHandActive())).usingItem;
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0))
    private boolean getIsHittingBlock(PlayerControllerMP playerControllerMP) {
        return Ares.EVENT_MANAGER.post(new InteractEvent(playerControllerMP.getIsHittingBlock())).usingItem;
    }

    @Inject(method = "processKeyBinds", at = @At("HEAD"))
    private void isHandActiveWrapper(CallbackInfo ci) {
        try {
            if(MC.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN
                    && MC.currentScreen == null
                    && Keyboard.getEventKeyState()
                    && Keyboard.getEventCharacter() != 0
                    && Command.PREFIX.getValue().length() >= 1
                    && Keyboard.getEventCharacter() == Command.PREFIX.getValue().charAt(0)) {
                MC.displayGuiScreen(new AresChatGUI(Command.PREFIX.getValue()));
            }
        } catch(Exception ignored) {
        }
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0, shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        profiler.startSection("aresTick");
        if(MC.player != null && MC.world != null) Module.tick();
        profiler.endSection();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void crash(CallbackInfo ci) {
        Ares.save();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo ci) {
        Ares.save();
    }
}
