package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.optimizations.CapeEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {
    @Shadow
    private NetworkPlayerInfo playerInfo;

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void onCape(CallbackInfoReturnable<ResourceLocation> cir) {
        Ares.EVENT_MANAGER.post(new CapeEvent(playerInfo, cir));
    }
}
