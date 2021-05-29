package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.global.UpdateHelper;
import dev.tigr.ares.fabric.AresMod;
import dev.tigr.ares.fabric.event.client.OpenScreenEvent;
import dev.tigr.ares.fabric.event.player.InteractEvent;
import dev.tigr.ares.fabric.gui.AresChatGUI;
import dev.tigr.ares.fabric.gui.AresMainMenu;
import dev.tigr.ares.fabric.gui.AresUpdateGUI;
import dev.tigr.ares.fabric.impl.modules.exploit.AirInteract;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    private final MinecraftClient MC = (MinecraftClient) (Object) this;

    @Shadow
    private Profiler profiler;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
    public void init(RunArgs runArgs, CallbackInfo ci) {
        Ares.initialize(AresMod.class);
    }

    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
    public void getWindowTitle(CallbackInfoReturnable<String> cir) {

        cir.setReturnValue(cir.getReturnValue().replaceAll("Minecraft", "Ares " + Ares.VERSION_FULL + " -").replaceAll("\\*", ""));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0, shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        profiler.push("aresTick");
        if(MC.player != null && MC.world != null) Module.tick();
        profiler.pop();
    }

    boolean checkupdate = true;
    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    public void openScreen(Screen screen, CallbackInfo ci) {
        // open update gui if update, else open main menu
        if(screen instanceof TitleScreen && !(screen instanceof AresMainMenu)) {
            ci.cancel();
            if(checkupdate && UpdateHelper.shouldUpdate()) {
                MC.openScreen(new AresUpdateGUI());
                checkupdate = false;
            }
            else MC.openScreen(new AresMainMenu());
        }

        // open chat gui if chat
        if(screen instanceof ChatScreen && screen.getClass() == ChatScreen.class && !MC.player.isSleeping()) {
            MC.openScreen(new AresChatGUI(ReflectionHelper.getPrivateValue(ChatScreen.class, screen, "originalChatText", "field_18973")));
            ci.cancel();
        }

        if(Ares.EVENT_MANAGER.post(new OpenScreenEvent(screen)).isCancelled()) ci.cancel();
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE"), cancellable = true)
    public void onKeyPressUse(CallbackInfo ci) {
        if(AirInteract.INSTANCE.getEnabled() && MC.player.getMainHandStack().getItem() instanceof BlockItem) ci.cancel();
    }

    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    public boolean breakBlockCheck(ClientPlayerEntity clientPlayerEntity) {
        return Ares.EVENT_MANAGER.post(new InteractEvent(clientPlayerEntity.isUsingItem())).usingItem;
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    public boolean useItemBreakCheck(ClientPlayerInteractionManager clientPlayerInteractionManager) {
        return Ares.EVENT_MANAGER.post(new InteractEvent(clientPlayerInteractionManager.isBreakingBlock())).usingItem;
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    public void crash(CallbackInfo info) {
        Ares.save();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo info) {
        Ares.save();
    }
}
