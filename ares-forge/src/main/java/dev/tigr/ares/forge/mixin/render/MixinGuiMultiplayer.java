package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
import dev.tigr.ares.core.gui.impl.menu.SelectionMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.realms.RealmsBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiMultiplayer.class)
public class MixinGuiMultiplayer extends GuiScreen implements Wrapper {
    SelectionMenu mainMenu;

    @Inject(method = "initGui", at = @At("HEAD"))
    private void initGui(CallbackInfo ci) {
        mainMenu = new SelectionMenu(MC.currentScreen.width, MC.currentScreen.height, MC.gameSettings.guiScale);

        mainMenu.getMenuButtonGroup().getButtons().forEach(button -> {
            if(button.getButtonName().equals("MP")) button.setPressed(true);
        });
    }

    @Inject(method = "drawScreen", at = @At("RETURN"), cancellable = true)
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        mainMenu.draw(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if(mouseButton == 0) {
            mainMenu.onClick(mouseX, mouseY);

            if(mainMenu.getCustomToggle().isPressed()) {
                for(MenuButton button: mainMenu.getMenuButtonGroup().getButtons()) {
                    if(button.isMouseOver(mouseX, mouseY)) {
                        if(button.getButtonName().equals("SP")) MC.displayGuiScreen(new GuiWorldSelection(this));
                        if(button.getButtonName().equals("MP")) MC.displayGuiScreen(new GuiMultiplayer(this));
                        if(button.getButtonName().equals("OP")) MC.displayGuiScreen(new GuiOptions(this, MC.gameSettings));
                        if(button.getButtonName().equals("RL")) {
                            RealmsBridge realmsBridge = new RealmsBridge();
                            realmsBridge.switchToRealms(this);
                        }
                    }
                }
            }
        }
    }
}
