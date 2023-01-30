package dev.tigr.ares.core.feature.module.modules.movement;

import dev.tigr.ares.core.event.movement.MovePlayerEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.entity.SelfUtils;
import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;

/**
 * @author Tigermouthbear
 * updated by Doogie
 * moved to core - Makrennel - 2021/12/17
 */
@Module.Info(name = "Speed", description = "Increase speed of the player", category = Category.MOVEMENT)
public class Speed extends Module {
    public static Speed INSTANCE;

    public Speed() {
        INSTANCE = this;
    }

    private final Setting<Mode> modeSetting = register(new EnumSetting<>("Mode", Mode.STRAFE));
    private final Setting<Boolean> strictSetting = register(new BooleanSetting("Strict", false).setVisibility(() -> modeSetting.getValue().equals(Mode.STRAFE)));

    enum Mode {
        Y_PORT,
        STRAFE
    }

    double lastDist = .2873;
    double speed = .2873;

    Mode lastMode = null;

    int phase = 0;

    public void onTick() {

        lastDist = Math.hypot(SELF.getPositionDelta().x, SELF.getPositionDelta().z);

        // 1.0888 timer
        UTILS.setTickLength(45.92212f);

        if (modeSetting.getValue() != lastMode) {
            lastMode = modeSetting.getValue();
            lastDist = speed = phase = 0;
        }

    }

    @EventHandler
    private final EventListener<MovePlayerEvent> moveEvent = new EventListener<>(Priority.HIGH, event -> {

        double v = .2873;
        double y = SELF.getVelocity().y;

        if (SELF.isPotionActive(1)) {
            final int amplifier = SELF.getPotionAmplifier(1);
            v *= 1.0 + 0.2 * (amplifier + 1);
        }

        switch (modeSetting.getValue()) {

            case STRAFE: {

                if (SELF.getInputMovementForward() == 0 && SELF.getInputMovementSideways() == 0) {
                    speed = phase = 0;
                    event.set(new V2D(0, 0));
                    event.setCancelled(true);
                    return;
                }

                if (SELF.isOnGround() || SELF.collidedHorizontally()) {
                    speed = phase = 0;
                }

                switch (phase) {

                    case 0: {

                        if (SELF.isOnGround()) {
                            y = .42;
                            speed = v * (strictSetting.getValue() ? 1.87 : 1.91);
                            phase++;
                        }

                        break;

                    }

                    case 1: {

                        speed -= .66 * v;
                        phase++;

                        break;

                    }

                    default: {

                        speed = lastDist - lastDist / 159;

                        break;

                    }

                }

                break;
            }

            case Y_PORT: {

                if (SELF.collidedHorizontally())
                    phase = 0;

                if (SELF.getInputMovementForward() == 0 && SELF.getInputMovementSideways() == 0) {
                    speed = phase = 0;
                    event.set(new V2D(0, 0));
                    event.setCancelled(true);
                    return;
                }

                switch (phase) {

                    case 0: {
                        phase++;
                        y = -1;
                        speed = v;
                        break;
                    }

                    case 1: {

                        speed *= 2.149;
                        y = .42;
                        phase++;

                        break;
                    }

                    case 2: {

                        speed = lastDist - .66 * (lastDist - v);
                        phase--;
                        y = -1;

                        break;
                    }

                }
            }
        }

        V2D dir = SelfUtils.getMovement(Math.max(v, speed));

        SELF.setVelocity(dir.a, y, dir.b);
        event.setX(SELF.getVelocity().x).setY(SELF.getVelocity().y).setZ(SELF.getVelocity().z);
        event.setCancelled(true);

    });

    @Override
    public void onDisable() {
        if(!Timer.INSTANCE.getEnabled())
            UTILS.setTickLength(50);
    }

    @Override
    public void onEnable() {
        speed = 0;
        lastDist = .2873;
        phase = 0;
    }

}
