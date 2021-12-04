package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.widget.ClickableWidget;
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
@Mixin(ClickableWidget.class)
public abstract class MixinClickableWidget implements Wrapper {
    @Shadow private Text message;
    @Shadow protected int width;
    @Shadow protected int height;
    @Shadow public int x;
    @Shadow public int y;

    @Shadow public abstract Text getMessage();

    @Shadow public abstract int getHeight();

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    public void drawButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) {
            RENDERER.drawRect(x, y, width, height, Color.BLACK);
            RENDERER.drawLineLoop(1, Color.ARES_RED,
                    x, y,
                    x + width, y,
                    x + width, y + height,
                    x, y + height
            );

            String text = message.getString();
            double textHeight =  height / 2D;
            double textWidth = FONT_RENDERER.getStringWidth(text, textHeight);
            FONT_RENDERER.drawStringWithCustomHeight(text, x + width / 2D - textWidth / 2D, y + height / 2D - textHeight / 2D, Color.WHITE, textHeight);
            ci.cancel();
        }
    }
}
