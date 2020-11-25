package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.movement.InventoryMoveEvent;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInputFromOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Tigermouthbear
 */
@Mixin(value = MovementInputFromOptions.class, priority = Integer.MAX_VALUE)
public class MixinMovementFromInputOptions {
    @Redirect(method = "updatePlayerMoveState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    public boolean isKeyDown(KeyBinding keyBinding) {
        InventoryMoveEvent event = Ares.EVENT_MANAGER.post(new InventoryMoveEvent());
        if(Minecraft.getMinecraft().player != null
                && Minecraft.getMinecraft().currentScreen != null
                && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)
                && event.getResult() == Result.ALLOW
                && !event.isCancelled()
        ) {
            return Keyboard.isKeyDown(keyBinding.getKeyCode());
        }
        return keyBinding.isKeyDown();
    }
}
