package dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.windows.modules.ModuleElement;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;

/**
 * GUI element for boolean setting
 *
 * @author Tigermouthbear 7/3/20
 */
public class BooleanSettingElement extends SettingElement<BooleanSetting> {
    public BooleanSettingElement(GUI gui, BooleanSetting setting) {
        super(gui, setting);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // if enabled render name in frames color else render in white
        FONT_RENDERER.drawStringWithCustomHeight(setting.getName(), getRenderX() + PADDING, getRenderY(), setting.getValue() ? ((ModuleElement) getParent()).getColor().getValue() : Color.WHITE, getHeight());
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) setting.setValue(!setting.getValue());

        super.click(mouseX, mouseY, mouseButton);
    }
}
