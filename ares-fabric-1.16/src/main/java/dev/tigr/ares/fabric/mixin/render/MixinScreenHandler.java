package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.client.ClickSlotEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler {
    @Inject(method = "onSlotClick", at = @At("INVOKE"))
    public void clickSlot(int i, int j, SlotActionType actionType, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        Ares.EVENT_MANAGER.post(new ClickSlotEvent(i, j, actionType, player));
    }
}
