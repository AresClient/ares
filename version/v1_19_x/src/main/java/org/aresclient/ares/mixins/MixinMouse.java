package org.aresclient.ares.mixins;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.MathHelper;
import org.aresclient.ares.Ares;
import org.aresclient.ares.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    private double realX = -1;
    private double realY = -1;

    @Shadow
    private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if(action == 0) {
                Ares.Companion.getEVENT_MANAGER().post(new InputEvent.Mouse.Released(button));
            } else {
                Ares.Companion.getEVENT_MANAGER().post(new InputEvent.Mouse.Pressed(button));
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            Ares.Companion.getEVENT_MANAGER().post(new InputEvent.Mouse.Scrolled(vertical));
        }
    }

    @Inject(method = "onCursorPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;updateMouse()V"))
    public void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            // we have to correct the mouse position because it increases above screen dimensions when cursor is locked
            double width = MinecraftClient.getInstance().getWindow().getWidth();
            double height = MinecraftClient.getInstance().getWindow().getHeight();
            if(realX == -1 || realY == -1) {
                realX = MathHelper.clamp(x, 0, width - 1);
                realY = MathHelper.clamp(y, 0, height - 1);
            }
            realX = MathHelper.clamp(realX + cursorDeltaX, 0, width - 1);
            realY = MathHelper.clamp(realY + cursorDeltaY, 0, height - 1);

            Ares.Companion.getEVENT_MANAGER().post(new InputEvent.Mouse.Moved(
                    realX, realY,
                    cursorDeltaX, cursorDeltaY
            ));
        }
    }
}