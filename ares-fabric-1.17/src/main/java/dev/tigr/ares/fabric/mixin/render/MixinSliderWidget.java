package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(SliderWidget.class)
public abstract class MixinSliderWidget extends MixinClickableWidget implements Wrapper {
    @Shadow protected double value;

    @Override
    public void drawButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) {
            RENDERER.drawRect(x, y, width, height, Color.BLACK);
            RENDERER.drawRect(x, y, value * width, height, Color.ARES_RED_LIGHT);
            RENDERER.drawLineLoop(2, Color.ARES_RED,
                    x, y,
                    x + width, y,
                    x + width, y + height,
                    x, y + height
            );

            String text = getMessage().getString();
            double textHeight = height / 2D;
            double textWidth = FONT_RENDERER.getStringWidth(text, textHeight);
            FONT_RENDERER.drawStringWithCustomHeight(text, x + width / 2D - textWidth / 2D, y + height / 2D - textHeight / 2D, Color.WHITE, textHeight);
            ci.cancel();
        }
    }
}
