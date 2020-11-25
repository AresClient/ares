package dev.tigr.ares.forge.mixin.render;

import com.google.common.base.Predicate;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.render.CameraClipEvent;
import dev.tigr.ares.forge.event.events.movement.PlayerTurnEvent;
import dev.tigr.ares.forge.event.events.player.AntiHitboxEvent;
import dev.tigr.ares.forge.event.events.render.HurtCamEvent;
import dev.tigr.ares.forge.event.events.render.SetupFogEvent;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
        if(Ares.EVENT_MANAGER.post(new AntiHitboxEvent()).getAllowed()) return new ArrayList<>();
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

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    public RayTraceResult rayTraceBlocks(WorldClient worldClient, Vec3d start, Vec3d end) {
        if(Ares.EVENT_MANAGER.post(new CameraClipEvent()).getResult() == Result.ALLOW) return null;
        else return worldClient.rayTraceBlocks(start, end);
    }

    @Inject(method = "setupFog", at = @At(value = "HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
        if(Ares.EVENT_MANAGER.post(new SetupFogEvent()).isCancelled()) callbackInfo.cancel();
    }
}
