package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.Potion;

/**
 * @author Tigermouthbear
 */

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

        if (((MC.player.moveForward != 0 || MC.player.moveStrafing != 0) && MC.player.onGround) && !(!forceJump.getValue() && modeSetting.getValue().equals(mode.STRAFE))) {

            if (MC.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                {
                    MC.player.motionY += (MC.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f + jump.getValue();
                }
            } else
                MC.player.motionY = jump.getValue();

        }

        if (MC.player.onGround)
            lastOG = true;

    }

    @Override
    public void onMotion() {

        if (modeSetting.getValue() == mode.YPORT) {

            if (MC.player.onGround)
                return;
            else
                MC.player.motionY = -jump.getValue() - 0.25;

            MC.player.motionX *= yportSpeed.getValue();
            MC.player.motionZ *= yportSpeed.getValue();

        } else if (modeSetting.getValue() == mode.STRAFE) {

            speedF *= getFric();

            double[] dir = WorldUtils.forward(Math.max(speedVal.getValue() * speedF * (getBaseMoveSpeed() / 0.2873), 0.2873));
            double[] gDir = WorldUtils.forward(0.2873 * groundVal.getValue());

            if (!MC.player.onGround) {
                MC.player.motionX = dir[0];
                MC.player.motionZ = dir[1];
            } else {
                speedF = 1;
                MC.player.motionX = gDir[0];
                MC.player.motionZ = gDir[1];
            }

        }

        if (!Timer.INSTANCE.getEnabled())
            ReflectionHelper.setPrivateValue(net.minecraft.util.Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), 45.955882352941176470588235294118f /* 1.088 timer speed */, "tickLength", "field_194149_e");

        if (MC.player.onGround)
            lastOG = true;
    }

    @Override
    public void onDisable() {
        if (!Timer.INSTANCE.getEnabled())
            ReflectionHelper.setPrivateValue(net.minecraft.util.Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), 1000.0F / 20.0F, "tickLength", "field_194149_e");
    }

    enum mode {YPORT, STRAFE}

    float getFric() {
         // bypass friction check
        float AIR_FRICTION = 0.98f;
        float WATER_FRICTION = 0.89f;
        float LAVA_FRICTION = 0.535f;

        if (MC.player.isInLava())
            return LAVA_FRICTION;
        if (MC.player.isInWater())
            return WATER_FRICTION;

        return AIR_FRICTION;

    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isPotionActive(Potion.getPotionById(1))) {
            final int amplifier = Minecraft.getMinecraft().player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

}