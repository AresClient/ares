package dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.api.TextField;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;

/**
 * GUI element for string setting
 *
 * @author Tigermouthbear 7/3/20
 */
public class StringSettingElement extends SettingElement<StringSetting> {
    private final TextField textField;

    public StringSettingElement(GUI gui, StringSetting setting) {
        super(gui, setting);

        textField = new TextField(gui, () -> Color.WHITE);
        textField.setX(() -> PADDING + FONT_RENDERER.getStringWidth(setting.getName() + " ", getHeight()));
        textField.setY(() -> getHeight() / 14);
        textField.setHeight(() -> getHeight() - getHeight() / 7);
        textField.setWidth(() -> getWidth() - FONT_RENDERER.getStringWidth(setting.getName(), getHeight()) - FONT_RENDERER.getStringWidth(" ", getHeight()));
        textField.setText(setting.getValue());
        add(textField);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // draw name
        FONT_RENDERER.drawStringWithCustomHeight(setting.getName(), getRenderX() + PADDING, getRenderY(), Color.WHITE, getHeight());

        // update setting value
        if(!textField.getText().equals(setting.getValue())) setting.setValue(textField.getText());
    }
}
