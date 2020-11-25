package dev.tigr.ares.core.feature.module;

import dev.tigr.ares.core.gui.impl.game.ClickGUI;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ClickGui", description = "Opens Ares Gui", category = Category.PLAYER, bind = "SEMICOLON")
public class ClickGUIMod extends Module {
    private static Color color = new Color(0, 0, 0, 1);
    private static ClickGUIMod INSTANCE;

    private final Setting<Float> red = register(new FloatSetting("Red", 0.7f, 0, 1));
    private final Setting<Float> green = register(new FloatSetting("Green", 0.03f, 0, 1));
    private final Setting<Float> blue = register(new FloatSetting("Blue", 0.03f, 0, 1));
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    public ClickGUIMod() {
        INSTANCE = this;
    }

    public static Color getColor() {
        color.setR(INSTANCE.red.getValue());
        color.setG(INSTANCE.green.getValue());
        color.setB(INSTANCE.blue.getValue());
        color.setA(1f);
        if(INSTANCE.rainbow.getValue()) color = IRenderer.rainbow().setA(1);

        return color;
    }

    @Override
    public void onEnable() {
        GUI_MANAGER.openGUI(ClickGUI.class);
        setEnabled(false);
    }
}
