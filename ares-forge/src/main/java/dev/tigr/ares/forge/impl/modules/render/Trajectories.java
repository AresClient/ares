package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.Vertex;
import dev.tigr.ares.forge.utils.TrajectoryUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear 10/4/20
 */
@Module.Info(name = "Trajectories", description = "Renders the projected path of throwables", category = Category.RENDER)
public class Trajectories extends Module {
    private final Setting<Boolean> box = register(new BooleanSetting("Block-Outline", false));
    private final Setting<Float> lineWeight = register(new FloatSetting("Line Weight", 3, 0, 12));

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
            List<Vertex> vertices = new ArrayList<>();
            for(Vec3d point: result.getPoints())
                vertices.add(new Vertex((float)point.x, (float)point.y, (float)point.z, color));

            RenderUtils.drawLineSeries(lineWeight.getValue(), vertices.toArray(new Vertex[0]));

            if(box.getValue()) {
                AxisAlignedBB bb = new AxisAlignedBB(new BlockPos(result.getHitVec()));
                float thickness = 6 * (48 / (float) MC.getRenderManager().renderViewEntity.getDistanceSq(bb.getCenter().x, bb.getCenter().y, bb.getCenter().z));
                if(thickness > 5) thickness = 5;
                if(thickness < 1) thickness = 1;

                GL11.glEnable(GL11.GL_BLEND);
                RenderUtils.cubeLines(bb, color, thickness);
            }

            RenderUtils.end3d();
        }
    }
}
