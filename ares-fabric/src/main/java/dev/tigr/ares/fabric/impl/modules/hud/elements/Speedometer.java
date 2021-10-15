package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;

/**
 * @author Tigermouthbear
 * Ported to Fabric by nwroot
 */
@Module.Info(name = "Speedometer", description = "Shows your speed", category = Category.HUD)
public class Speedometer extends HudElement {
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));
    private final Setting<Boolean> average = register(new BooleanSetting("Average", true));
    private final Setting<Integer> tickInterval = register(new IntegerSetting("Update Interval", 10, 1, 50));
    private final Setting<SpeedUnits> speedUnit = register(new EnumSetting<>("Unit", SpeedUnits.KILOMETERS_PH));

    enum SpeedUnits {
        METERS_PS,
        MILES_PH,
        KILOMETERS_PH
    }

    private double speedTotal = 0;
    private double speed = 0;

    public Speedometer() {
        super(150, 60, 0, 0); // FONT_RENDERER.getFontHeight() is broken when initializing on Fabric
    }

    @Override
    public void onTick() {
        if(MC.player == null || MC.world == null) return;

        if(!average.getValue() && TICKS % tickInterval.getValue() != 0) return;

        double localSpeed;
        if(!MC.player.isAlive()) {
            speed = 0;
            return;
        }

        float tickLength = ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).getTickTime();
        double tps = tickLength / 1000;

        double xMove = MC.player.getPos().x - MC.player.prevX;
        double zMove = MC.player.getPos().z - MC.player.prevZ;
        localSpeed = Math.sqrt(xMove * xMove + zMove * zMove) / tps;
        speedTotal += localSpeed;

        if(!average.getValue()) speed = localSpeed;
        else if(TICKS % tickInterval.getValue() == 0) {
            speed = speedTotal / tickInterval.getValue();
            speedTotal = 0;
        }
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
        setHeight(FONT_RENDERER.getFontHeight());
    }
}
