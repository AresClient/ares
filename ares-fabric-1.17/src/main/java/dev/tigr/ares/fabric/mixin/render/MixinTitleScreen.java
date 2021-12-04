package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MainMenuGUI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen implements Wrapper {
    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addMCButton(CallbackInfo ci) {
        addDrawableChild(new ButtonWidget(2, 2, 98, 20, new LiteralText("Ares Main Menu"), (buttonWidget) -> {
            GUI_MANAGER.openGUI(MainMenuGUI.class);
            ClickGUIMod.toggleCustomMenu();
        }));

        if(ClickGUIMod.shouldRenderCustomMenu()) {
            GUI_MANAGER.openGUI(MainMenuGUI.class);
        }
    }
}
