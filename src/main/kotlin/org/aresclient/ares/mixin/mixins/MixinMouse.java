package org.aresclient.ares.mixin.mixins;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse implements JWrapper {
    @Unique private double realX = -1;
    @Unique private double realY = -1;

    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Unique private static IntArraySet pressed = new IntArraySet();

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if(action == 0) {
                EVENTS.post(new InputEvent.Mouse.Released(button));
                pressed.remove(button);
            } else {
                var isRepeat = pressed.contains(button);
                EVENTS.post(new InputEvent.Mouse.Pressed(button, isRepeat));
                if (!isRepeat) pressed.add(button);
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if(EVENTS.post(new InputEvent.Mouse.Scrolled(vertical)).isCancelled()) ci.cancel();
        }
    }

//    @Inject(method = "onCursorPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;updateMouse()V"))
//    public void onCursorPos(long window, double x, double y, CallbackInfo ci) {
//        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
//            // we have to correct the mouse position because it increases above screen dimensions when cursor is locked
//            double width = MinecraftClient.getInstance().getWindow().getWidth();
//            double height = MinecraftClient.getInstance().getWindow().getHeight();
//            if(realX == -1 || realY == -1) {
//                realX = MathHelper.clamp(x, 0, width - 1);
//                realY = MathHelper.clamp(y, 0, height - 1);
//            }
//            realX = MathHelper.clamp(realX + cursorDeltaX, 0, width - 1);
//            realY = MathHelper.clamp(realY + cursorDeltaY, 0, height - 1);
//
//            EVENTS.post(new InputEvent.Mouse.Moved(
//                    realX, realY,
//                    cursorDeltaX, cursorDeltaY
//            ));
//        }
//    }
}
