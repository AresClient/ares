package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.render.CrosshairRenderEvent;
import dev.tigr.ares.fabric.mixin.accessors.DrawableHelperAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear 1/4/21
 */
@Module.Info(name = "DebugCrosshair", description = "Show f3 crosshair", category = Category.RENDER)
public class DebugCrosshair extends Module {
    @EventHandler
    public EventListener<CrosshairRenderEvent> crosshairRenderEvent = new EventListener<>(event -> {
        event.setCancelled(true);
        int zLevel = ((DrawableHelperAccessor) MC.inGameHud).getZOffset();
        DebugCrosshair.renderCrosshair(MC.getTickDelta(), MC.getWindow().getScaledWidth(), MC.getWindow().getScaledHeight(), (float) zLevel);
    });

    private static void renderCrosshair(float partialTicks, int width, int height, float zLevel) {
        GameOptions gameOptions = MC.options;

        if(gameOptions.getPerspective().isFirstPerson() && (!MC.player.isSpectator() || shouldRenderSpectatorCrosshair(MC.crosshairTarget)) && !gameOptions.hudHidden) {
            // TODO: render fix
            //RenderSystem.pushMatrix();
            //RenderSystem.translatef((float)(width / 2), (float)(height / 2), zLevel);
            Camera camera = MC.gameRenderer.getCamera();
            //RenderSystem.rotatef(camera.getPitch(), -1.0F, 0.0F, 0.0F);
            //RenderSystem.rotatef(camera.getYaw(), 0.0F, 1.0F, 0.0F);
            //RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
            //RenderSystem.renderCrosshair(10);
            //RenderSystem.popMatrix();
        }
    }

    private static boolean shouldRenderSpectatorCrosshair(HitResult hitResult) {
        if(hitResult == null) {
            return false;
        } else if(hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)hitResult).getEntity() instanceof NamedScreenHandlerFactory;
        } else if(hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            return MC.world.getBlockState(blockPos).createScreenHandlerFactory(MC.world, blockPos) != null;
        } else return false;
    }
}
