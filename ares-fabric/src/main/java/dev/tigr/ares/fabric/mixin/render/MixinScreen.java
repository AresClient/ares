package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.ItemTooltipEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear 12/10/20
 */
@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
    public void renderTooltipPre(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new ItemTooltipEvent.Pre(matrices, stack, x, y));
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("RETURN"))
    public void renderTooltipPost(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new ItemTooltipEvent.Post(matrices, stack, x, y));
    }
}
