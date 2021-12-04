package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MainMenuGUI;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends GuiScreen implements Wrapper {
    @Inject(method = "initGui", at = @At("RETURN"))
    private void addMCButton(CallbackInfo ci) {
        buttonList.add(new GuiButton(69, 2, 2, 98, 20, "Ares Main Menu"));

        if(ClickGUIMod.shouldRenderCustomMenu()) {
            GUI_MANAGER.openGUI(MainMenuGUI.class);
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if(button.id == 69) {
            ClickGUIMod.toggleCustomMenu();
            Ares.GUI_MANAGER.openGUI(MainMenuGUI.class);
        }
    }
}
