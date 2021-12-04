package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.SelectionMenuGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiWorldSelection.class)
public class MixinGuiWorldSelection extends GuiScreen implements Wrapper {
    private static final SelectionMenuGUI SELECTION_MENU = new SelectionMenuGUI();

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) SELECTION_MENU.draw(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) SELECTION_MENU.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    public void mouseReleased(int mouseX, int mouseY, int state, CallbackInfo ci) {
        if(ClickGUIMod.shouldRenderCustomMenu()) SELECTION_MENU.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onResize(Minecraft client, int width, int height) {
        GuiScreen screen = (GuiScreen) GUI_MANAGER;
        screen.width = width;
        screen.height = height;
        super.onResize(client, width, height);
    }
}
