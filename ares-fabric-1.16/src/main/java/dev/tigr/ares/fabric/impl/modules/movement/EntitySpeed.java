package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.fabric.event.client.UpdateLivingEntityEvent;
import dev.tigr.ares.fabric.utils.entity.EntityUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "EntitySpeed", description = "Change speed of riding entities", category = Category.MOVEMENT)
public class EntitySpeed extends Module {
    private final Setting<Boolean> fly = register(new BooleanSetting("Fly", false));
    private final Setting<Double> speed = register(new DoubleSetting("Speed", 2, 1, 10));
    private final Setting<Boolean> onGround = register(new BooleanSetting("onGround", false));

    @EventHandler
    public EventListener<UpdateLivingEntityEvent.Post> updateLivingEntityPost = new EventListener<>(event -> {
        if(MC.player == null) return;
        if(MC.player.getRootVehicle() != null && event.getEntity() == MC.player.getRootVehicle() && MC.player.isRiding()) {
            MC.player.getRootVehicle().yaw = MC.player.yaw;
            MC.player.getRootVehicle().setVelocity(0, fly.getValue() ? 0 : MC.player.getRootVehicle().getVelocity().y, 0);

            if(onGround.getValue()) MC.player.getRootVehicle().distanceTraveled = speed.getValue().intValue();
            EntityUtils.moveEntityWithSpeed(MC.player.getVehicle(), speed.getValue(), fly.getValue());
        }
    });
}
