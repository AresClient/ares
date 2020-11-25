package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.forge.event.events.movement.PlayerTurnEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "RotationLock", description = "Prevents player from rotating camera", category = Category.PLAYER)
public class RotationLock extends Module {
    private final Setting<Direction> mode = register(new EnumSetting<>("Direction", Direction.CUSTOM));
    @EventHandler
    public EventListener<PlayerTurnEvent> playerTurnEvent = new EventListener<>(event -> {
        event.setPitch(0);
        event.setYaw(0);
    });

    @Override
    public void onTick() {
        switch(mode.getValue()) {
            case SOUTH:
                MC.player.rotationYaw = 0;
                break;
            case WEST:
                MC.player.rotationYaw = 90;
                break;
            case NORTH:
                MC.player.rotationYaw = 180;
                break;
            case EAST:
                MC.player.rotationYaw = -90;
                break;
        }
    }

    enum Direction {CUSTOM, NORTH, EAST, SOUTH, WEST}
}
