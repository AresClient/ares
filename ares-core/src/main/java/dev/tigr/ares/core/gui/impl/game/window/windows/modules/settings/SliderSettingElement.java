package dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.windows.modules.ModuleElement;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.setting.settings.numerical.NumberSetting;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * GUI element for number setting
 *
 * @param <T> type extending {@link NumberSetting}
 * @author Tigermouthbear 7/3/20
 */
public class SliderSettingElement<T extends NumberSetting<?>> extends SettingElement<T> {
    private boolean dragging = false;
    private int sliderPos = -1;

    public SliderSettingElement(GUI gui, T setting) {
        super(gui, setting);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        if(sliderPos == -1)
            sliderPos = (int) (((setting.getValue().doubleValue() - setting.getMin().doubleValue()) / (setting.getMax().doubleValue() - setting.getMin().doubleValue())) * getWidth());

        // set value based on mouse drag
        if(dragging) {
            double pos = Utils.clamp(mouseX - getRenderX(), 0, getWidth());
            double val = (pos / getWidth()) * (setting.getMax().doubleValue() - setting.getMin().doubleValue()) + setting.getMin().doubleValue();

            sliderPos = (int) pos;

            if(setting instanceof FloatSetting) ((FloatSetting) setting).setValue((float) val);
            else if(setting instanceof DoubleSetting) ((DoubleSetting) setting).setValue(val);
            else if(setting instanceof IntegerSetting) ((IntegerSetting) setting).setValue((int) val);
        }

        // draw slider background based on value
        RENDERER.drawRect(getRenderX(), getRenderY(), sliderPos, getHeight(), ((ModuleElement) getParent()).getColor().getValue().setA(0.5f));

        // draw name and value
        FONT_RENDERER.drawStringWithCustomHeight(setting.getName() + ": " + Utils.roundDouble(setting.getValue().doubleValue(), 2), getRenderX() + PADDING, getRenderY(), Color.WHITE, getHeight());
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        super.click(mouseX, mouseY, mouseButton);

        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) dragging = true;
    }

    @Override
    public void release(int mouseX, int mouseY, int mouseButton) {
        super.release(mouseX, mouseY, mouseButton);

        if(mouseButton == 0) dragging = false;
    }
}