package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
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

    public Text retrieveMessage() {
        return message;
    }

    MenuButton replacement;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(int x, int y, int width, int height, Text string, CallbackInfo ci) {
        replacement = new MenuButton(message.getString());
        replacement.setX(x);
        replacement.setY(y);
        replacement.setWidth(width);
        replacement.setHeight(height);
    }

    @Inject(method = "renderButton", at = @At("RETURN"))
    public void drawButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ClickGUIMod.INSTANCE.customButtons.getValue()) {
            RENDERER.drawRect(replacement.getRenderX(), replacement.getRenderY(), replacement.getWidth(), replacement.getHeight(), new Color(0,0,0,1));
            replacement.setButtonName(message.getString());
            replacement.draw(mouseX, mouseY, delta);
        }
    }
}
