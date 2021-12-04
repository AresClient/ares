package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import net.minecraft.util.math.Direction;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
 */
@Module.Info(name = "Coordinates", description = "Display coordinates in HUD", category = Category.HUD)
public class Coordinates extends HudElement {
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    public Coordinates() {
        super(100, 100, 10, 5);
    }

    public void draw() {
        Direction enumfacing = MC.player.getHorizontalFacing();
        String overworld;
        String nether;
        String direction = enumfacing.name() + " " + getDirection(enumfacing) + " (" + Utils.roundDouble(MC.player.getYaw(), 1) + ", " + Utils.roundDouble(MC.player.getPitch(), 1) + ")";

        if(MC.world.getDimension().hasCeiling() && !MC.world.getDimension().isBedWorking()) {
            overworld = "x" + Utils.roundDouble(MC.player.getX() * 8, 1) + ", y" + Utils.roundDouble(MC.player.getY(), 1) + ", z" + Utils.roundDouble(MC.player.getZ() * 8, 1);
            nether = "(x" + Utils.roundDouble(MC.player.getX(), 1) + ", y" + Utils.roundDouble(MC.player.getY(), 1) + ", z" + Utils.roundDouble(MC.player.getZ(), 1) + ")";
        } else {
            overworld = "x" + Utils.roundDouble(MC.player.getX(), 1) + ", y" + Utils.roundDouble(MC.player.getY(), 1) + ", z" + Utils.roundDouble(MC.player.getZ(), 1);
            nether = "(x" + Utils.roundDouble(MC.player.getX() / 8, 1) + ", y" + Utils.roundDouble(MC.player.getY(), 1) + ", z" + Utils.roundDouble(MC.player.getZ() / 8, 1) + ")";
        }

        drawString(overworld, getX(), getY(), rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
        drawString(nether, getX(), getY() + FONT_RENDERER.getFontHeight() + 2, rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
        drawString(direction, getX(), getY() + FONT_RENDERER.getFontHeight() * 2 + 4, rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);

        double max = FONT_RENDERER.getStringWidth(overworld);
        max = Math.max(FONT_RENDERER.getStringWidth(nether), max);
        max = Math.max(FONT_RENDERER.getStringWidth(direction), max);

        setWidth((int) (max + 1));
        setHeight(FONT_RENDERER.getFontHeight() * 3 + 4);
    }

    private String getDirection(Direction enumfacing) {
        String direction = "ERROR";
        switch(enumfacing) {
            case NORTH:
                direction = "-Z";
                break;
            case SOUTH:
                direction = "+Z";
                break;
            case WEST:
                direction = "-X";
                break;
            case EAST:
                direction = "+X";
                break;
        }
        return direction;
    }
}
