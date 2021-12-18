package dev.tigr.ares.core.feature.module.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.entity.SelfUtils;
import dev.tigr.ares.core.util.math.doubles.V2D;

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

    public final Setting<Float> jump = register(new FloatSetting("Jump Height", 0.42f, 0.38f, 0.44f));
    private final Setting<mode> modeSetting = register(new EnumSetting<mode>("Mode", mode.STRAFE));
    private final Setting<Boolean> forceJump = register(new BooleanSetting("Jump", true)).setVisibility(() -> modeSetting.getValue().equals(mode.STRAFE));
    private final Setting<Float> speedVal = register(new FloatSetting("Strafe Speed", 0.38f, 0.2f, 0.6f)).setVisibility(() -> modeSetting.getValue() == mode.STRAFE);
    private final Setting<Float> groundVal = register(new FloatSetting("Ground Speed", 1f, 1f, 2f)).setVisibility(() -> modeSetting.getValue() == mode.STRAFE);
    private final Setting<Float> yportSpeed = register(new FloatSetting("YPort Speed", 1.75f, 0.1f, 3)).setVisibility(() -> !(modeSetting.getValue() == mode.STRAFE));

    float speedF;
    boolean lastOG;

    public void onTick() {
        if(((SELF.getInputMovementForward() != 0 || SELF.getInputMovementSideways() != 0) && SELF.isOnGround())
                && !(!forceJump.getValue() && modeSetting.getValue().equals(mode.STRAFE))) {
            if(SELF.isPotionActive(8))
                SELF.setVelocity(
                        SELF.getVelocity().x,
                        (SELF.getPotionAmplifier(8) + 1) * 0.1f + jump.getValue(),
                        SELF.getVelocity().z
                );
            else
                SELF.setVelocity(
                        SELF.getVelocity().x,
                        jump.getValue(),
                        SELF.getVelocity().z
                );
        }

        if(SELF.isOnGround()) lastOG = true;
    }

    @Override
    public void onMotion() {
        if(modeSetting.getValue() == mode.YPORT) {
            if(SELF.isOnGround()) return;
            else SELF.setVelocity(SELF.getVelocity().x, -jump.getValue() - 0.25, SELF.getVelocity().z);

            SELF.setVelocity(
                    SELF.getVelocity().x * yportSpeed.getValue(),
                    SELF.getVelocity().y,
                    SELF.getVelocity().z * yportSpeed.getValue()
            );
        } else if (modeSetting.getValue() == mode.STRAFE) {
            speedF *= getFric();

            V2D dir = SelfUtils.getMovement(Math.max(speedVal.getValue() * speedF * (getBaseMoveSpeed() / 0.15321), 0.15321));
            V2D gDir = SelfUtils.getMovement(0.2873 * groundVal.getValue());

            if(!SELF.isOnGround()) SELF.setVelocity(dir.a, SELF.getVelocity().y, dir.b);
            else {
                speedF = 1;
                SELF.setVelocity(gDir.a, SELF.getVelocity().y, gDir.b);
            }
        }

        if(!Timer.INSTANCE.getEnabled())
            UTILS.setTpsMultiplier(1.088F);

        if(SELF.isOnGround())
            lastOG = true;
    }

    @Override
    public void onDisable() {
        if(!Timer.INSTANCE.getEnabled())
            UTILS.setTickLength(50);
    }

    enum mode {YPORT, STRAFE}

    float getFric() {
        // bypass friction check
        float AIR_FRICTION = 0.98f;
        float WATER_FRICTION = 0.89f;
        float LAVA_FRICTION = 0.535f;

        if (SELF.isInLava())
            return LAVA_FRICTION;
        if (SELF.isInWater())
            return WATER_FRICTION;

        return AIR_FRICTION;

    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.15321;
        if(SELF.isPotionActive(1)) {
            final int amplifier = SELF.getPotionAmplifier(1);
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }
}
