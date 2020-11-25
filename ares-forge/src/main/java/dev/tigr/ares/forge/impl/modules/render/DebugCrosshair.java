package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.render.CrosshairRenderEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "DebugCrosshair", description = "Show f3 crosshair", category = Category.RENDER)
public class DebugCrosshair extends Module {
    @EventHandler
    public EventListener<CrosshairRenderEvent> crosshairRenderEvent = new EventListener<>(event -> {
        event.setCancelled(true);
        int width = new ScaledResolution(MC).getScaledWidth();
        int height = new ScaledResolution(MC).getScaledHeight();
        float zLevel = ReflectionHelper.getPrivateValue(Gui.class, MC.ingameGUI, "zLevel", "field_73735_i");
        DebugCrosshair.renderCrosshair(event.getPartialTicks(), width, height, zLevel);
    });

    private static void renderCrosshair(float partialTicks, int width, int height, float zLevel) {
        GameSettings gamesettings = MC.gameSettings;

        if(gamesettings.thirdPersonView == 0) {
            if(MC.playerController.isSpectator() && MC.pointedEntity == null) {
                RayTraceResult raytraceresult = MC.objectMouseOver;

                if(raytraceresult == null || raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
                    return;
                }

                BlockPos blockpos = raytraceresult.getBlockPos();

                net.minecraft.block.state.IBlockState state = MC.world.getBlockState(blockpos);
                if(!state.getBlock().hasTileEntity(state) || !(MC.world.getTileEntity(blockpos) instanceof IInventory)) {
                    return;
                }
            }

            if(!gamesettings.hideGUI) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (width / 2), (float) (height / 2), zLevel);
                Entity entity = MC.getRenderViewEntity();
                if(entity != null) {
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
                    GlStateManager.scale(-1.0F, -1.0F, -1.0F);
                    OpenGlHelper.renderDirections(10);
                }
                GlStateManager.popMatrix();
            }
        }
    }
}
