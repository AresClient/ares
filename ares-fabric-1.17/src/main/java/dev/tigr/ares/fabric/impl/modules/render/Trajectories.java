package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.Vertex;
import dev.tigr.ares.fabric.utils.TrajectoryUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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

            Color color = result.getType() == HitResult.Type.ENTITY ? Color.RED : Color.WHITE;
            List<Vertex> vertices = new ArrayList<>();
            for(Vec3d point: result.getPoints())
                vertices.add(new Vertex((float)point.x, (float)point.y, (float)point.z, color));

            RenderUtils.drawLineSeries(lineWeight.getValue(), vertices.toArray(new Vertex[0]));

            if(box.getValue()) {
                Box bb = new Box(new BlockPos(result.getHitVec()));
                float thickness = 6 * (48 / (float) MC.cameraEntity.squaredDistanceTo(bb.getCenter().x, bb.getCenter().y, bb.getCenter().z));
                if(thickness > 8) thickness = 8;
                if(thickness < 1.68) thickness = 1.68f;

                RenderUtils.cubeLines(bb, color, thickness);
            }

            RenderUtils.end3d();
        }
    }
}
