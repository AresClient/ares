package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import dev.tigr.ares.core.gui.impl.menu.MainMenu;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen implements Wrapper {
    @Shadow protected abstract void switchToRealms();

    MainMenu mainMenu;

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void initGui(CallbackInfo ci) {
        mainMenu = new MainMenu(MC.currentScreen.width, MC.currentScreen.height, new Color(0,0,0,1), MC.options.guiScale);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addMCButton(CallbackInfo ci) {
        addDrawableChild(new ButtonWidget(2, 2, 98, 20, new LiteralText("Account Manager"), (buttonWidget) -> Ares.GUI_MANAGER.openGUI(AccountManagerGUI.class)));
    }

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    public void drawScreen(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        mainMenu.draw(mouseX, mouseY, delta);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if(mouseButton == 0) {
            mainMenu.onClick(mouseX, mouseY);

            if(mainMenu.getCustomToggle().isPressed()) {
                for(MenuButton button: mainMenu.getMenuButtonGroup().getButtons()) {
                    if(button.isMouseOver(mouseX, mouseY)) {
                        if(button.getButtonName().equals("Singleplayer")) MC.setScreen(new SelectWorldScreen(this));
                        if(button.getButtonName().equals("Multiplayer")) MC.setScreen(new MultiplayerScreen(this));
                        if(button.getButtonName().equals("Options")) MC.setScreen(new OptionsScreen(this, MC.options));
                        if(button.getButtonName().equals("Realms")) switchToRealms();
                    }
                }

                cir.cancel();
            }
        }
    }
}
