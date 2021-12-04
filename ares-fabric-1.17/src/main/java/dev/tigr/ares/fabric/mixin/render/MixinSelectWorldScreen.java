package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.SelectionMenuGUI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(SelectWorldScreen.class)
public class MixinSelectWorldScreen extends Screen implements Wrapper {
    private static final SelectionMenuGUI SELECTION_MENU = new SelectionMenuGUI();

    protected MixinSelectWorldScreen(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void drawScreen(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) SELECTION_MENU.draw(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(ClickGUIMod.shouldRenderCustomMenu()) SELECTION_MENU.mouseClicked((int) mouseX, (int) mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if(ClickGUIMod.shouldRenderCustomMenu()) SELECTION_MENU.mouseReleased((int) mouseX, (int) mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
