package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptionSlider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiOptionSlider.class)
public class MixinGuiOptionSlider extends MixinGuiButton {
    @Shadow private float sliderValue;
    @Shadow @Final private float minValue;
    @Shadow @Final private float maxValue;

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) {
            double sliderPos = ((sliderValue - minValue) / (maxValue - minValue)) * width;
            RENDERER.drawRect(x, y, width, height, Color.BLACK);
            RENDERER.drawRect(x, y, sliderPos, height, Color.ARES_RED_LIGHT);
            RENDERER.drawLineLoop(2, Color.ARES_RED,
                    x, y,
                    x + width, y,
                    x + width, y + height,
                    x, y + height
            );

            double textHeight = height / 2D;
            double textWidth = FONT_RENDERER.getStringWidth(displayString, textHeight);
            FONT_RENDERER.drawStringWithCustomHeight(displayString, x + width / 2D - textWidth / 2D, y + height / 2D - textHeight / 2D, Color.WHITE, textHeight);
            ci.cancel();
        }
    }
}
