package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.event.movement.MovePlayerEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.utils.HoleType;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tigermouthbear 12/13/20
 */
@Module.Info(name = "Anchor", description = "Pulls you into holes fast", category = Category.COMBAT)
public class Anchor extends Module {
    private final Setting<Integer> max = register(new IntegerSetting("Max Distance", 5, 1, 20));
    private final Setting<Integer> speed = register(new IntegerSetting("Speed", 10, 5, 20));
    private final Setting<Integer> cutoff = register(new IntegerSetting("Pitch Cutoff", 18, -90, 90));

    @EventHandler
    public EventListener<MovePlayerEvent> movePlayerEvent = new EventListener<>(event -> {
        // if over hole
        if(event.getMoverType() .equals("SELF") && MC.player != null && MC.player.getPitch() > cutoff.getValue()
                && isOverHole(MC.player.getPos()) && MC.player.getVelocity().y <= 0.1) {
            // correct movement
            event.set(getBounds(MC.player.getX()) * 0.2, isCenter(MC.player.getPos()) ? -speed.getValue() : event.getY(), getBounds(MC.player.getZ()) * 0.2);
            event.setCancelled(true);
        }
    });

    private boolean isOverHole(Vec3d vec3d) {
        BlockPos pos = new BlockPos(vec3d).down();
        int num = 0;

        while(WorldUtils.isHole(pos) == HoleType.NONE) {
            pos = pos.down();
            if(num++ >= max.getValue() || !MC.world.getBlockState(pos).isAir()) return false;
        }
        return true;
    }

    private boolean isCenter(Vec3d vec3d) {
        return isCenter(vec3d.x) && isCenter(vec3d.z);
    }

    private boolean isCenter(double pos) {
        double val = pos - Math.floor(pos);
        return val > 0.3 && val < 0.7;
    }

    private int getBounds(double pos) {
        double val = pos - Math.floor(pos);
        return val < 0.3 ? 1 : (val > 0.7 ? -1 : 0);
    }
}
