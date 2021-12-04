package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.fabric.event.render.CameraClipEvent;
import dev.tigr.ares.fabric.mixin.accessors.CameraAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

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
        if(modifyDistance.getValue()) event.setDesiredCameraDistance(distance.getValue());

        Camera camera = MC.gameRenderer.getCamera();

        if(clip.getValue()) return;

        for(int i = 0; i < 8; ++i) {
            float f = (float)((i & 1) * 2 - 1);
            float g = (float)((i >> 1 & 1) * 2 - 1);
            float h = (float)((i >> 2 & 1) * 2 - 1);
            f *= 0.1F;
            g *= 0.1F;
            h *= 0.1F;
            Vec3d vec3d = camera.getPos().add(f, g, h);
            Vec3d vec3d2 = new Vec3d(camera.getPos().x - (double)camera.getHorizontalPlane().getX() * event.getDesiredCameraDistance() + (double)f + (double)h, camera.getPos().y - (double)camera.getHorizontalPlane().getY() * event.getDesiredCameraDistance() + (double)g, camera.getPos().z - (double)camera.getHorizontalPlane().getZ() * event.getDesiredCameraDistance() + (double)h);
            HitResult hitResult = ((CameraAccessor) camera).getArea().raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, camera.getFocusedEntity()));
            if (hitResult.getType() != HitResult.Type.MISS) {
                double d = hitResult.getPos().distanceTo(camera.getPos());
                if (d < event.getDesiredCameraDistance()) {
                    event.setDesiredCameraDistance(d);
                }
            }
        }
    });
}
