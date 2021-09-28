package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
import dev.tigr.ares.core.gui.impl.menu.SelectionMenu;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsBridgeScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen implements Wrapper {
    SelectionMenu mainMenu;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void initGui(CallbackInfo ci) {
        mainMenu = new SelectionMenu(MC.currentScreen.width, MC.currentScreen.height, MC.options.guiScale);

        mainMenu.getMenuButtonGroup().getButtons().forEach(button -> {
            if(button.getButtonName().equals("MP")) button.setPressed(true);
        });
    }

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    public void drawScreen(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        mainMenu.draw(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0) {
            mainMenu.onClick(mouseX, mouseY);

            if(mainMenu.getCustomToggle().isPressed()) {
                for(MenuButton button: mainMenu.getMenuButtonGroup().getButtons()) {
                    if(button.isMouseOver(mouseX, mouseY)) {
                        if(button.getButtonName().equals("SP")) MC.openScreen(new SelectWorldScreen(this));
                        if(button.getButtonName().equals("MP")) MC.openScreen(new MultiplayerScreen(this));
                        if(button.getButtonName().equals("OP")) MC.openScreen(new OptionsScreen(this, MC.options));
                        if(button.getButtonName().equals("RL")) {
                            RealmsBridgeScreen realmsBridgeScreen = new RealmsBridgeScreen();
                            realmsBridgeScreen.switchToRealms(this);
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
