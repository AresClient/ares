package dev.tigr.ares.core.feature.module;

import dev.tigr.ares.core.event.client.PostInitializationEvent;
import dev.tigr.ares.core.gui.impl.game.ClickGUI;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.font.GlyphFont;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

import static dev.tigr.ares.core.Ares.ARIAL_FONT;
import static dev.tigr.ares.core.Ares.MONO_FONT;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ClickGui", description = "Opens Ares Gui", category = Category.PLAYER, bind = "SEMICOLON", alwaysListening = true)
public class ClickGUIMod extends Module {
    private static Color color = new Color(0, 0, 0, 1);
    private static ClickGUIMod INSTANCE;

    enum Font {
        MONO(MONO_FONT), ARIAL(ARIAL_FONT);

        private final GlyphFont glyphFont;

        Font(GlyphFont glyphFont) {
            this.glyphFont = glyphFont;
        }
    }

    private final Setting<Font> font = register(new EnumSetting<>("Font", Font.MONO));
    private final Setting<Float> red = register(new FloatSetting("Red", 0.7f, 0, 1));
    private final Setting<Float> green = register(new FloatSetting("Green", 0.03f, 0, 1));
    private final Setting<Float> blue = register(new FloatSetting("Blue", 0.03f, 0, 1));
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    public ClickGUIMod() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<PostInitializationEvent> postInitializationEvent = new EventListener<>(event -> {
        FONT_RENDERER.setFont(font.getValue().glyphFont);
    });

    @Override
    public void onEnable() {
        GUI_MANAGER.openGUI(ClickGUI.class);
        setEnabled(false);
    }

    @Override
    public void onTick() {
        FONT_RENDERER.setFont(font.getValue().glyphFont);
    }

    public static Color getColor() {
        color.setR(INSTANCE.red.getValue());
        color.setG(INSTANCE.green.getValue());
        color.setB(INSTANCE.blue.getValue());
        color.setA(1f);
        if(INSTANCE.rainbow.getValue()) color = IRenderer.rainbow().setA(1);

        return color;
    }
}
