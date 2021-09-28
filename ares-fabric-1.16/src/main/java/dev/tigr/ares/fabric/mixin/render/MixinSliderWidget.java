package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MenuSlider;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(SliderWidget.class)
public class MixinSliderWidget extends MixinAbstractButtonWidget implements Wrapper {
    @Shadow protected double value;
    MenuSlider replacement;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(int x, int y, int width, int height, Text text, double value, CallbackInfo ci) {
        replacement = new MenuSlider(retrieveMessage().getString());
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(width);
        replacement.setHeight(height);
    }

    @Override
    public void drawButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ClickGUIMod.INSTANCE.customButtons.getValue()) {
            replacement.setButtonName(retrieveMessage().getString());
            RENDERER.drawRect(replacement.getRenderX(), replacement.getRenderY(), replacement.getWidth(), replacement.getHeight(), new Color(0,0,0,1));
            replacement.setValues((float) value, 0, 1);
            replacement.draw(mouseX, mouseY, delta);
        }
    }
}
