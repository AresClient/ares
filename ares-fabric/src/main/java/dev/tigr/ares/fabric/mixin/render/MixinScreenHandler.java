package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.client.ClickSlotEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler {
    @Inject(method = "onSlotClick", at = @At("INVOKE"))
    public void clickSlot(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new ClickSlotEvent(slotIndex, button, actionType, player));
    }
}
