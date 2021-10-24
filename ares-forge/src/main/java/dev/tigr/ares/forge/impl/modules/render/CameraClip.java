package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.forge.event.events.render.CameraClipEvent;
import dev.tigr.ares.forge.mixin.accessor.EntityRendererAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "CameraClip", description = "Allows the 3rd person camera to go through walls", category = Category.RENDER)
public class CameraClip extends Module {
    private final Setting<Boolean> clip = register(new BooleanSetting("Clip", true));
    private final Setting<Boolean> modifyDistance = register(new BooleanSetting("Modify Distance", true));
    private final Setting<Double> distance = register(new DoubleSetting("Distance", 3.5, 0, 10)).setVisibility(modifyDistance::getValue);

    @EventHandler
    public EventListener<CameraClipEvent> cameraClipEvent = new EventListener<>(event -> {
        if(!(clip.getValue() || modifyDistance.getValue())) return;

        event.setCancelled(true);

        Entity entity = MC.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)event.getTickDelta();
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)event.getTickDelta() + (double)f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)event.getTickDelta();

        if(entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping()) {
            f = (float)((double)f + 1.0D);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if(!MC.gameSettings.debugCamEnable) {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = MC.world.getBlockState(blockpos);
                net.minecraftforge.client.ForgeHooksClient.orientBedCamera(MC.world, blockpos, iblockstate, entity);

                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * event.getTickDelta() + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * event.getTickDelta(), -1.0F, 0.0F, 0.0F);
            }
        }
        else if(MC.gameSettings.thirdPersonView > 0) {
            double d3 = modifyDistance.getValue() ? distance.getValue() : ((EntityRendererAccessor) MC.entityRenderer).getThirdPersonDistancePrev() + (4.0F - ((EntityRendererAccessor) MC.entityRenderer).getThirdPersonDistancePrev()) * event.getTickDelta();

            if(MC.gameSettings.debugCamEnable) GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
            else {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if(MC.gameSettings.thirdPersonView == 2) {
                    f2 += 180.0F;
                }

                double d4 = (double)(-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
                double d5 = (double)(MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
                double d6 = (double)(-MathHelper.sin(f2 * 0.017453292F)) * d3;

                for(int i = 0; i < 8; ++i) {
                    float f3 = (float)((i & 1) * 2 - 1);
                    float f4 = (float)((i >> 1 & 1) * 2 - 1);
                    float f5 = (float)((i >> 2 & 1) * 2 - 1);
                    f3 = f3 * 0.1F;
                    f4 = f4 * 0.1F;
                    f5 = f5 * 0.1F;
                    RayTraceResult raytraceresult = clip.getValue() ? null : MC.world.rayTraceBlocks(new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));

                    if(raytraceresult != null) {
                        double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

                        if(d7 < d3) d3 = d7;
                    }
                }

                if(MC.gameSettings.thirdPersonView == 2) GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        }
        else GlStateManager.translate(0.0F, 0.0F, 0.05F);

        if(!MC.gameSettings.debugCamEnable) {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * event.getTickDelta() + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * event.getTickDelta();
            float roll = 0.0F;
            if(entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal)entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * event.getTickDelta() + 180.0F;
            }
            IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(MC.world, entity, event.getTickDelta());
            net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup forgeEvent = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup(MC.entityRenderer, entity, state, event.getTickDelta(), yaw, pitch, roll);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(forgeEvent);
            GlStateManager.rotate(forgeEvent.getRoll(), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(forgeEvent.getPitch(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(forgeEvent.getYaw(), 0.0F, 1.0F, 0.0F);
        }

        GlStateManager.translate(0.0F, -f, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)event.getTickDelta();
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)event.getTickDelta() + (double)f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)event.getTickDelta();
        ((EntityRendererAccessor) MC.entityRenderer).setCloudFog(MC.renderGlobal.hasCloudFog(d0, d1, d2, event.getTickDelta()));
    });
}
