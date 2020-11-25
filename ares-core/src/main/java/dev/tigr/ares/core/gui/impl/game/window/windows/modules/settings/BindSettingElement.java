package dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.setting.settings.BindSetting;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.KEYBOARD_MANAGER;

/**
 * GUI element for bind setting
 *
 * @author Tigermouthbear 7/3/20
 */
public class BindSettingElement extends SettingElement<BindSetting> {
    private boolean listening = false;

    public BindSettingElement(GUI gui, BindSetting setting) {
        super(gui, setting);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // draw value and name
        FONT_RENDERER.drawStringWithCustomHeight(listening ? setting.getName() + ": ..." : setting.getName() + ": " + setting.getValue(), getRenderX() + PADDING, getRenderY(), Color.WHITE, getHeight());
    }

    @Override
    public void keyTyped(Character typedChar, int keyCode) {
        if(listening) {
            setting.setValue(KEYBOARD_MANAGER.getKeyName(keyCode).toUpperCase());
            listening = false;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(isMouseOver(mouseX, mouseY)) {
            if(mouseButton == 0) listening = !listening;
            if(mouseButton == 1 && listening) {
                setting.setValue("NONE");
                listening = false;
            }
        }
    }
}
