package dev.tigr.ares.forge.mixin.render;

import com.google.common.base.Predicate;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.movement.PlayerTurnEvent;
import dev.tigr.ares.forge.event.events.player.AntiHitboxEvent;
import dev.tigr.ares.forge.event.events.render.*;
import dev.tigr.ares.forge.utils.Reimplementations;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity p_getEntitiesInAABBexcluding_1_, AxisAlignedBB p_getEntitiesInAABBexcluding_2_, Predicate<? super Entity> p_getEntitiesInAABBexcluding_3_) {
        if(Ares.EVENT_MANAGER.post(new AntiHitboxEvent()).getResult() == Result.ALLOW) return new ArrayList<>();
        return worldClient.getEntitiesInAABBexcluding(p_getEntitiesInAABBexcluding_1_, p_getEntitiesInAABBexcluding_2_, p_getEntitiesInAABBexcluding_3_);
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
    private void onTurnPlayerWrapper(EntityPlayerSP entityPlayerSP, float yaw, float pitch) {
        PlayerTurnEvent turnEvent = Ares.EVENT_MANAGER.post(new PlayerTurnEvent(pitch, yaw));
        entityPlayerSP.turn(turnEvent.getYaw(), turnEvent.getPitch());
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCamera(float ticks, CallbackInfo info) {
        if(Ares.EVENT_MANAGER.post(new HurtCamEvent()).isCancelled()) info.cancel();
    }

    @Inject(method = "orientCamera", at = @At("HEAD"), cancellable = true)
    public void orientCamera(float partialTicks, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new CameraClipEvent(partialTicks)).isCancelled()) ci.cancel();
    }

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
        if(Ares.EVENT_MANAGER.post(new SetupFogEvent()).isCancelled()) callbackInfo.cancel();
    }

    @Redirect(method = "updateLightmap", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;gammaSetting:F"))
    public float getGammaSetting(GameSettings gameSettings) {
        return Ares.EVENT_MANAGER.post(new GammaEvent(gameSettings.gammaSetting)).getGamma();
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V"))
    public void renderItem(ItemRenderer itemRenderer, float partialTicks) {
        if(Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Invoke()).isCancelled()) {
            Reimplementations.renderItemInFirstPerson(itemRenderer, partialTicks);
        } else {
            itemRenderer.renderItemInFirstPerson(partialTicks);
        }
    }
}
