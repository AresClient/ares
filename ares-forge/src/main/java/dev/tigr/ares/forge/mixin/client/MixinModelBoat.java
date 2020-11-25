package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.forge.impl.modules.movement.BoatFly;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.tigr.ares.Wrapper.MC;

// credit: KAMI-blue
@Mixin(ModelBoat.class)
public class MixinModelBoat {
    @Inject(method = "render", at = @At("HEAD"))
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        if(MC.player.getRidingEntity() == entityIn && BoatFly.INSTANCE.getEnabled()) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, BoatFly.INSTANCE.getOpacity());
            GlStateManager.enableBlend();
        }
    }
}
