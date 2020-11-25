package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "EntitySpeed", description = "Change speed of riding entities", category = Category.MOVEMENT)
public class EntitySpeed extends Module {
    private final Setting<Boolean> fly = register(new BooleanSetting("Fly", false));
    private final Setting<Double> speed = register(new DoubleSetting("Speed", 2, 1, 10));
    private final Setting<Boolean> onGround = register(new BooleanSetting("onGround", false));

    @EventHandler
    public EventListener<LivingEvent.LivingUpdateEvent> livingUpdateEvent = new EventListener<>(event -> {
        if(MC.player.isRiding() && MC.player.getRidingEntity() != null) {
            MC.player.getRidingEntity().rotationYaw = MC.player.rotationYaw;
            MC.player.getRidingEntity().setVelocity(0, fly.getValue() ? 0 : MC.player.getRidingEntity().motionY, 0);

            if(onGround.getValue()) {
                MC.player.getRidingEntity().distanceWalkedModified = speed.getValue().intValue();
            }
            WorldUtils.moveEntityWithSpeed(MC.player.getRidingEntity(), speed.getValue(), fly.getValue());
        }
    });
}
