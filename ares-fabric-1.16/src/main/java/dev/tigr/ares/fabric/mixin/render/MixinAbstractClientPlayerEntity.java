package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.CapeEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 9/8/20
 */
@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {
    @Shadow PlayerListEntry cachedScoreboardEntry;

    @Inject(method = "getCapeTexture", at = @At("RETURN"), cancellable = true)
    public void getCape(CallbackInfoReturnable<Identifier> cir) {
        Ares.EVENT_MANAGER.post(new CapeEvent(cachedScoreboardEntry, cir));
    }
}
