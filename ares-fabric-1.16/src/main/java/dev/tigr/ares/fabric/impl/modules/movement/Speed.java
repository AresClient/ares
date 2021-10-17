package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.entity.effect.StatusEffect;

@Module.Info(name = "Speed", description = "Increase speed of the player", category = Category.MOVEMENT)
public class Speed extends Module {

    public final Setting<Float> jump = register(new FloatSetting("Jump Height", 0.42f, 0.38f, 0.44f));
    private final Setting<mode> modeSetting = register(new EnumSetting<mode>("Mode", mode.STRAFE));
    private final Setting<Boolean> forceJump = register(new BooleanSetting("Jump", true)).setVisibility(() -> modeSetting.getValue().equals(mode.STRAFE));
    private final Setting<Float> speedVal = register(new FloatSetting("Strafe Speed", 0.38f, 0.2f, 0.6f)).setVisibility(() -> modeSetting.getValue() == mode.STRAFE);
    private final Setting<Float> groundVal = register(new FloatSetting("Ground Speed", 1f, 1f, 2f)).setVisibility(() -> modeSetting.getValue() == mode.STRAFE);
    private final Setting<Float> yportSpeed = register(new FloatSetting("YPort Speed", 1.75f, 0.1f, 3)).setVisibility(() -> !(modeSetting.getValue() == mode.STRAFE));
    float speedF;
    boolean lastOG;

    public void onTick() {

        if (MC.player == null || MC.world == null)
            return;

        if (((MC.options.keyForward.isPressed() || MC.options.keyBack.isPressed() || MC.options.keyLeft.isPressed() || MC.options.keyRight.isPressed())
                && MC.player.isOnGround()) && !(!forceJump.getValue() && modeSetting.getValue().equals(mode.STRAFE))) {

            if (MC.player.getActiveStatusEffects().containsValue(MC.player.getStatusEffect(StatusEffect.byRawId(8)))) {
                {
                    MC.player.setVelocity(MC.player.getVelocity().x,(MC.player.getStatusEffect(StatusEffect.byRawId(8)).getAmplifier() + 1) * 0.1f + jump.getValue(), MC.player.getVelocity().z);
                    //MC.player.motionY += (MC.player.getStatusEffect(StatusEffect.byRawId(8)).getAmplifier() + 1) * 0.1f + jump.getValue();
                }
            } else
                MC.player.setVelocity(MC.player.getVelocity().x,jump.getValue(), MC.player.getVelocity().z);

        }

        if (MC.player.isOnGround())
            lastOG = true;

    }

    @Override
    public void onMotion() {

        if (modeSetting.getValue() == mode.YPORT) {

            if (MC.player.isOnGround())
                return;
            else
                MC.player.setVelocity(MC.player.getVelocity().x,-jump.getValue() - 0.25, MC.player.getVelocity().z);
            //MC.player.motionY = -jump.getValue() - 0.25;

/*          MC.player.motionX *= yportSpeed.getValue();
            MC.player.motionZ *= yportSpeed.getValue();*/
            MC.player.setVelocity(MC.player.getVelocity().x * yportSpeed.getValue(), MC.player.getVelocity().y, MC.player.getVelocity().z * yportSpeed.getValue());

        } else if (modeSetting.getValue() == mode.STRAFE) {

            speedF *= getFric();

            double[] dir = SelfUtils.getMovement(Math.max(speedVal.getValue() * speedF * (getBaseMoveSpeed() / 0.2873), 0.2873));
            double[] gDir = SelfUtils.getMovement(0.2873 * groundVal.getValue());

            if (!MC.player.isOnGround()) {
                MC.player.setVelocity(dir[0], MC.player.getVelocity().y, dir[1]);
            } else {
                speedF = 1;
                MC.player.setVelocity(gDir[0], MC.player.getVelocity().y, gDir[1]);
            }

        }

        if (!Timer.INSTANCE.getEnabled())
            ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(45.955882352941176470588235294118f /* 1.088 timer speed */);

        if (MC.player.isOnGround())
            lastOG = true;
    }

    @Override
    public void onDisable() {
        if (!Timer.INSTANCE.getEnabled())
            ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(1000.0F / 20);
    }

    enum mode {YPORT, STRAFE}

    float getFric() {
        // bypass friction check
        float AIR_FRICTION = 0.98f;
        float WATER_FRICTION = 0.89f;
        float LAVA_FRICTION = 0.535f;

        if (MC.player.isInLava())
            return LAVA_FRICTION;
        if (MC.player.isSubmergedInWater())
            return WATER_FRICTION;

        return AIR_FRICTION;

    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (MC.player.getActiveStatusEffects().containsValue(MC.player.getStatusEffect(StatusEffect.byRawId(1)))) {
            final int amplifier = MC.player.getStatusEffect(StatusEffect.byRawId(1)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

}
