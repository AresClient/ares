package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import dev.tigr.ares.core.gui.impl.menu.MainMenu;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen implements Wrapper {
    @Shadow protected abstract void switchToRealms();

    MainMenu mainMenu;

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    private void initGui(CallbackInfo ci) {
        mainMenu = new MainMenu(MC.currentScreen.width, MC.currentScreen.height, new Color(0,0,0,1), MC.gameSettings.guiScale);
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void addMCButton(CallbackInfo ci) {
        buttonList.add(new GuiButton(69, 2, 2, 98, 20, "Account Manager"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if(button.id == 69) Ares.GUI_MANAGER.openGUI(AccountManagerGUI.class);
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
                        if(button.getButtonName().equals("Singleplayer")) MC.displayGuiScreen(new GuiWorldSelection(this));
                        if(button.getButtonName().equals("Multiplayer")) MC.displayGuiScreen(new GuiMultiplayer(this));
                        if(button.getButtonName().equals("Options")) MC.displayGuiScreen(new GuiOptions(this, MC.gameSettings));
                        if(button.getButtonName().equals("Realms")) switchToRealms();
                    }
                }

                ci.cancel();
            }
        }
    }
}
