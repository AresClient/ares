package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.utils.RenderUtils;
import dev.tigr.ares.forge.utils.TrajectoryUtils;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/**
 * @author Tigermouthbear 10/4/20
 */
@Module.Info(name = "Trajectories", description = "Renders the projected path of throwables", category = Category.RENDER)
public class Trajectories extends Module {
    private final Setting<Boolean> box = register(new BooleanSetting("Block-Outline", false));

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
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);

            Color color = result.getType() == RayTraceResult.Type.ENTITY ? Color.RED : Color.WHITE;
            Vec3d prevPoint = null;
            for(Vec3d point: result.getPoints()) {
                if(prevPoint != null) RENDERER.drawLine(point.x, point.y, point.z, prevPoint.x, prevPoint.y, prevPoint.z, 2, color);
                prevPoint = point;
            }

            if(box.getValue()) {
                GL11.glEnable(GL11.GL_BLEND);
                RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(new BlockPos(result.getHitVec())), 1, 0, 0, 0.6f);
            }

            RenderUtils.end3d();
        }
    }
}
