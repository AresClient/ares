package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.event.events.movement.MovePlayerEvent;
import dev.tigr.ares.forge.utils.HoleType;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
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
        if(event.getMoverType() == MoverType.SELF && MC.player != null && MC.player.rotationPitch > cutoff.getValue()
                && isOverHole(MC.player.getPositionVector()) && MC.player.motionY <= 0.1) {
            event.setShouldDo(true);
            // correct movement
            event.set(getBounds(MC.player.posX) * 0.2, isCenter(MC.player.getPositionVector()) ? -speed.getValue() : event.getY(), getBounds(MC.player.posZ) * 0.2);
        } else event.setShouldDo(false);
    });

    private boolean isOverHole(Vec3d vec3d) {
        BlockPos pos = new BlockPos(vec3d).down();
        int num = 0;

        while(WorldUtils.isHole(pos) == HoleType.NONE) {
            pos = pos.down();
            if(num++ >= max.getValue() || MC.world.getBlockState(pos).getBlock() != Blocks.AIR) return false;
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
