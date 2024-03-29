package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import net.minecraft.entity.item.EntityMinecartChest;

/**
 * @author UberRipper
 */
@Module.Info(name = "MinecartCount",
        description = "Lists the amount of minecarts in your render distance, useful for hunting stashes",
        category = Category.HUD)
public class MinecartCount extends HudElement {
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));
    private final Setting<Integer> minimumNotificationNumber
            = register(new IntegerSetting("Min count ", 1, 1, 20));

    public MinecartCount() {
        super(100, 60, 0, FONT_RENDERER.getFontHeight());
    }

    public void draw() {
        final int chests = (int) MC.world.loadedEntityList.stream()
                .filter(Entity -> Entity instanceof EntityMinecartChest).count();
        if (minimumNotificationNumber.getValue() <= chests) {
            final String text = chests + " minecart chests";
            drawString(text, getX(), getY(), rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
            setWidth((int) FONT_RENDERER.getStringWidth(text) + 1);
        }
    }
}
