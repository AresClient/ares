package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MenuSlider;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiOptionSlider.class)
public class MixinGuiOptionSlider extends MixinGuiButton {
    @Shadow private float sliderValue;
    @Shadow @Final private float minValue;
    @Shadow @Final private float maxValue;

    MenuSlider replacement;

    @Inject(method = "<init>(IIILnet/minecraft/client/settings/GameSettings$Options;)V", at = @At("RETURN"))
    public void onInit(int buttonId, int x, int y, GameSettings.Options optionIn, CallbackInfo ci) {
        replacement = new MenuSlider(optionIn.name());
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(width);
        replacement.setHeight(height);
    }

    @Inject(method = "<init>(IIILnet/minecraft/client/settings/GameSettings$Options;FF)V", at = @At("RETURN"))
    public void onInit(int buttonId, int x, int y, GameSettings.Options optionIn, float minValueIn, float maxValue, CallbackInfo ci) {
        replacement = new MenuSlider(optionIn.name());
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(width);
        replacement.setHeight(height);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if(ClickGUIMod.INSTANCE.customButtons.getValue()) {
            replacement.setButtonName(displayString);
            RENDERER.drawRect(replacement.getRenderX(), replacement.getRenderY(), replacement.getWidth(), replacement.getHeight(), new Color(0,0,0,1));
            replacement.setValues(sliderValue, minValue, maxValue);
            replacement.draw(mouseX, mouseY, partialTicks);
        }
    }
}
