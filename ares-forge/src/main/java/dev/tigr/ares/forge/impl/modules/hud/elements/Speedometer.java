package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Speedometer", description = "Shows your speed", category = Category.HUD)
public class Speedometer extends HudElement {
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));
    private final Setting<SpeedUnits> speedUnit = register(new EnumSetting<>("Unit", SpeedUnits.KILOMETERS_PH));
    
    enum SpeedUnits {
        METERS_PS,
        MILES_PH,
        KILOMETERS_PH
    }

    private double speed = 0;

    public Speedometer() {
        super(150, 60, 0, FONT_RENDERER.getFontHeight());
    }

    @Override
    public void onTick() {
        if(MC.player.ticksExisted % 10 != 0) return;

        float tickLength = ReflectionHelper.getPrivateValue(Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), "tickLength", "field_194149_e");
        double tps = tickLength / 1000;

        double xMove = MC.player.posX - MC.player.prevPosX;
        double zMove = MC.player.posZ - MC.player.prevPosZ;
        speed = Math.sqrt(xMove * xMove + zMove * zMove) / tps;
    }

    public void draw() {
        String str;
        switch(speedUnit.getValue()) {
            case METERS_PS:
                str = Utils.roundDouble(speed, 1) + " M/S";
                break;
            case MILES_PH:
                str = Utils.roundDouble(speed * 2.23694, 1) + " MPH";
                break;
            default:
                str = Utils.roundDouble(speed * 3.6, 1) + " KM/H";
        }

        drawString(str, getX(), getY(), rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
        setWidth((int) FONT_RENDERER.getStringWidth(str) + 1);
    }
}
