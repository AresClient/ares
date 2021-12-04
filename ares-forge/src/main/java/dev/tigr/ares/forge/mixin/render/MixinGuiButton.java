package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiButton.class)
public class MixinGuiButton implements Wrapper {
    @Shadow public String displayString;
    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) {
            RENDERER.drawRect(x, y, width, height, Color.BLACK);
            RENDERER.drawLineLoop(1, Color.ARES_RED,
                    x, y,
                    x + width, y,
                    x + width, y + height,
                    x, y + height
            );

            double textHeight =  height / 2D;
            double textWidth = FONT_RENDERER.getStringWidth(displayString, textHeight);
            FONT_RENDERER.drawStringWithCustomHeight(displayString, x + width / 2D - textWidth / 2D, y + height / 2D - textHeight / 2D, Color.WHITE, textHeight);
            ci.cancel();
        }
    }
}
