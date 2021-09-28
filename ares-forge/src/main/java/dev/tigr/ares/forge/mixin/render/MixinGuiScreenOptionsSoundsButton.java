package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MenuSlider;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(targets = "net.minecraft.client.gui.GuiScreenOptionsSounds$Button")
public class MixinGuiScreenOptionsSoundsButton extends MixinGuiButton {
    @Shadow public float volume;
    MenuSlider replacement;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(GuiScreenOptionsSounds outer, int buttonId, int x, int y, SoundCategory categoryIn, boolean master, CallbackInfo ci) {
        replacement = new MenuSlider(displayString);
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(width);
        replacement.setHeight(height);
        replacement.setValues(volume, 0, 1);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if(ClickGUIMod.INSTANCE.customButtons.getValue()) {
            replacement.setButtonName(displayString);
            RENDERER.drawRect(replacement.getRenderX(), replacement.getRenderY(), replacement.getWidth(), replacement.getHeight(), new Color(0,0,0,1));
            replacement.setVal(volume);
            replacement.draw(mouseX, mouseY, partialTicks);
        }
    }
}
