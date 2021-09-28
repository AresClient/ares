package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
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

    @Shadow public int width;
    @Shadow public int height;
    MenuButton replacement;

    @Inject(method = "<init>(IIILjava/lang/String;)V", at = @At("RETURN"))
    public void onInit(int buttonId, int x, int y, String buttonText, CallbackInfo ci) {
        replacement = new MenuButton(displayString);
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(200);
        replacement.setHeight(20);
    }

    @Inject(method = "<init>(IIIIILjava/lang/String;)V", at = @At("RETURN"))
    public void onInit(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, CallbackInfo ci) {
        replacement = new MenuButton(displayString);
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(widthIn);
        replacement.setHeight(heightIn);
    }

    @Inject(method = "drawButton", at = @At("RETURN"))
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if(ClickGUIMod.INSTANCE.customButtons.getValue()) {
            RENDERER.drawRect(replacement.getRenderX(), replacement.getRenderY(), replacement.getWidth(), replacement.getHeight(), new Color(0,0,0,1));
            replacement.setButtonName(displayString);
            replacement.draw(mouseX, mouseY, partialTicks);
        }
    }
}
