package dev.tigr.ares.fabric.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.utils.RenderUtils;
import dev.tigr.ares.fabric.utils.TrajectoryUtils;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tigermouthbear 10/4/20
 */
@Module.Info(name = "Trajectories", description = "Renders the projected path of throwables", category = Category.RENDER)
public class Trajectories extends Module {
    private final Setting<Boolean> box = register(new BooleanSetting("Block-Outline", true));

    private TrajectoryUtils.Result result = null;

    @Override
    public void onTick() {
        TrajectoryUtils.Projectiles projectile = TrajectoryUtils.getProjectile(MC.player);
        if(projectile != null) result = TrajectoryUtils.calculate(MC.player, projectile);
        else result = null;
    }

    @Override
    public void onRender3d() {
        if(result != null) {
            RenderUtils.prepare3d();
            RenderSystem.disableBlend();
            RenderSystem.disableLighting();

            Color color = result.getType() == HitResult.Type.ENTITY ? Color.RED : Color.WHITE;
            Vec3d prevPoint = null;
            for(Vec3d point: result.getPoints()) {
                if(prevPoint != null) RENDERER.drawLine(point.x, point.y, point.z, prevPoint.x, prevPoint.y, prevPoint.z, 2, color);
                prevPoint = point;
            }

            if(box.getValue()) {
                RenderSystem.enableBlend();
                RenderUtils.renderSelectionBoundingBox(new Box(new BlockPos(result.getHitVec())), 1, 0, 0, 0.6f);
            }

            RenderUtils.end3d();
        }
    }
}
