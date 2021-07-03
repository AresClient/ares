package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.movement.InventoryMoveEvent;
import dev.tigr.ares.fabric.event.movement.SlowDownEvent;
import dev.tigr.ares.fabric.mixin.accessors.KeyBindingAccessor;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    KeyboardInput keyboardInput = (KeyboardInput) (Object) this;

    @Inject(method = "tick", at = @At("RETURN"))
    public void isKeyDown(boolean flag, CallbackInfo ci) {
        InventoryMoveEvent event = Ares.EVENT_MANAGER.post(new InventoryMoveEvent());
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc.player != null
                && mc.currentScreen != null
                && !(mc.currentScreen instanceof ChatScreen)
                && event.getResult() == Result.ALLOW
                && !event.isCancelled()
        ) {
            keyboardInput.pressingForward = inertiaBypass(mc.options.keyForward);
            keyboardInput.pressingBack = inertiaBypass(mc.options.keyBack);
            keyboardInput.pressingLeft = inertiaBypass(mc.options.keyLeft);
            keyboardInput.pressingRight = inertiaBypass(mc.options.keyRight);
            keyboardInput.movementForward = keyboardInput.pressingForward == keyboardInput.pressingBack ? 0.0F : (keyboardInput.pressingForward ? 1.0F : -1.0F);
            keyboardInput.movementSideways = keyboardInput.pressingLeft == keyboardInput.pressingRight ? 0.0F : (keyboardInput.pressingLeft ? 1.0F : -1.0F);
            keyboardInput.jumping = inertiaBypass(mc.options.keyJump);
            keyboardInput.sneaking = inertiaBypass(mc.options.keySneak);
            if(flag) {
                keyboardInput.movementSideways = (float)((double)keyboardInput.movementSideways * 0.3D);
                keyboardInput.movementForward = (float)((double)keyboardInput.movementForward * 0.3D);
            }
        }
    }

    // i rlly have to manually do this to be compatible with inertia :(
    private boolean inertiaBypass(KeyBinding keyBinding) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(),
                ((KeyBindingAccessor) keyBinding).getBoundKey().getCode());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void endofTick(boolean shouldSlowdown, CallbackInfo ci) {
        if(shouldSlowdown && Ares.EVENT_MANAGER.post(new SlowDownEvent()).isCancelled()) {
            keyboardInput.movementSideways = (float)((double)keyboardInput.movementSideways / 0.3D);
            keyboardInput.movementForward = (float)((double)keyboardInput.movementForward / 0.3D);
        }
    }
}
