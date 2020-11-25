package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.fabric.event.movement.PlayerTurnEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 */
@Module.Info(name = "RotationLock", description = "Prevents player from rotating camera", category = Category.PLAYER)
public class RotationLock extends Module {
    private final Setting<Direction> mode = register(new EnumSetting<>("Direction", Direction.CUSTOM));

    @EventHandler
    public EventListener<PlayerTurnEvent> playerTurnEvent = new EventListener<>(event -> event.setCancelled(true));

    @Override
    public void onTick() {
        switch(mode.getValue()) {
            case SOUTH:
                MC.player.yaw = 0;
                break;
            case WEST:
                MC.player.yaw = 90;
                break;
            case NORTH:
                MC.player.yaw = 180;
                break;
            case EAST:
                MC.player.yaw = -90;
                break;
        }
    }

    enum Direction {CUSTOM, NORTH, EAST, SOUTH, WEST}
}
