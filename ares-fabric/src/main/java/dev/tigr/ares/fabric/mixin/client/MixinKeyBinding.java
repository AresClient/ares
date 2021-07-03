package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.setting.settings.BindSetting;
import dev.tigr.ares.fabric.event.client.SetKeyBindingStateEvent;
import dev.tigr.ares.fabric.gui.AresChatGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear 8/25/20
 */
@Mixin(KeyBinding.class)
public class MixinKeyBinding {
    @Inject(method = "onKeyPressed", at = @At("HEAD"))
    private static void onKeyPress(InputUtil.Key key, CallbackInfo ci) {
        String keyName = Ares.KEYBOARD_MANAGER.getKeyName(key.getCode());

        if(!keyName.equalsIgnoreCase("NONE")) {
            for(BindSetting setting: BindSetting.getBinds()) {
                if(keyName.equalsIgnoreCase(setting.getValue())) setting.invoke();
            }

            if(String.valueOf((char)key.getCode()).equalsIgnoreCase(Command.PREFIX.getValue())) MinecraftClient.getInstance().openScreen(new AresChatGUI(""));
        }
    }

    @Inject(method = "setKeyPressed", at = @At("HEAD"))
    private static void setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new SetKeyBindingStateEvent(key, pressed));
    }
}
