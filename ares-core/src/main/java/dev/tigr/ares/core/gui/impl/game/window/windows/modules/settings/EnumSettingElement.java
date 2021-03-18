package dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;

/**
 * GUI element for enum setting
 *
 * @author Tigermouthbear 7/3/20
 */
public class EnumSettingElement<T extends Enum<?>> extends SettingElement<EnumSetting<T>> {
    public EnumSettingElement(GUI gui, EnumSetting<T> setting) {
        super(gui, setting);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // render name and value
        FONT_RENDERER.drawStringWithCustomHeight(setting.getName() + ": " + setting.getValue().name(), getRenderX() + PADDING, getRenderY(), Color.WHITE, getHeight());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void click(int mouseX, int mouseY, int mouseButton) {
        // if clicked, shift value
        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            for(int z = 0; z < setting.getModes().length; z++) {
                if(setting.getModes()[z].equals(setting.getValue())) {
                    if(z == 0) {
                        setting.setValue((T) setting.getModes()[setting.getModes().length - 1]);
                        break;
                    }
                    setting.setValue((T) setting.getModes()[z - 1]);
                    break;
                }
            }
        }
    }
}
