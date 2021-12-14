package dev.tigr.ares.core.feature.module.modules.movement;

import dev.tigr.ares.core.event.movement.MovePlayerEvent;
import dev.tigr.ares.core.event.movement.PlayerJumpEvent;
import dev.tigr.ares.core.event.movement.SetPlayerSprintEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.SelfUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;

/**
 * @author Tigermouthbear
 * @author Makrennel - rewrite and moved to core 2021/12/11
 */
@Module.Info(name = "AutoSprint", description = "Makes player always sprint in any direction", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    public static AutoSprint INSTANCE;

    public AutoSprint() {
        INSTANCE = this;
    }

    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.OMNIDIRECTIONAL));

    enum Mode { OMNIDIRECTIONAL, VANILLA, STRAFE }

    // Using the MovePlayerEvent instead of setVelocity in the onMotion method keeps the speed the same in every direction
    @EventHandler
    private final EventListener<MovePlayerEvent> onMovePlayer = new EventListener<>(Priority.DEFAULT, event -> {
        if(mode.getValue() == Mode.VANILLA) {
            SELF.setSprinting(true);
            return;
        }

        if(SELF.getInputMovementForward() == 0 && SELF.getInputMovementSideways() == 0) return;

        if(event.getMoverType().equals("SELF")) {
            if(SELF.isInLava() || SELF.isInWater()) {
                SELF.setSprinting(true);
                return;
            }

            // We don't want to client side sprint because the jump boost will be too fast and flag AC
            else SELF.setSprinting(false);
            if(!SELF.isOnGround() && mode.getValue() == Mode.OMNIDIRECTIONAL) return;

            event
                    .set(SelfUtils.getMovement(getBaseMoveSpeed()))
                    .setCancelled(true);
        }
    });

    // Allows omnidirectional to apply a small speed boost on jump in any direction
    @EventHandler
    private final EventListener<PlayerJumpEvent> onPlayerJump = new EventListener<>(event -> {
        if(mode.getValue() == Mode.OMNIDIRECTIONAL)
            SELF.addVelocity(SelfUtils.getMovement(0.26));
    });

    // Allows vanilla mode to sprint in any direction while on ground
    @EventHandler
    private final EventListener<SetPlayerSprintEvent> onSetPlayerSprint = new EventListener<>(event -> {
        if(mode.getValue() == Mode.VANILLA || SELF.isInWater() || SELF.isInLava())
            event
                    .setSprinting(true)
                    .setCancelled(true);
    });

    private static double getBaseMoveSpeed() {
        double baseSpeed = 0.28061673f;
        if(SELF.isPotionActive(1)) {
            final int amplifier = SELF.getPotionAmplifier(1);
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }
}
