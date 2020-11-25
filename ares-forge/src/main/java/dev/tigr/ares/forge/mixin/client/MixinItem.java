package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.player.OnItemUsePass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(Item.class)
public abstract class MixinItem {
    //For Obfuscation
    private static final Minecraft MC = Minecraft.getMinecraft();

    @Inject(method = "onItemUse", at = @At(value = "RETURN"), cancellable = true)
    public void onItemUse(EntityPlayer p_onItemUse_1_, World p_onItemUse_2_, BlockPos p_onItemUse_3_, EnumHand p_onItemUse_4_, EnumFacing p_onItemUse_5_, float p_onItemUse_6_, float p_onItemUse_7_, float p_onItemUse_8_, CallbackInfoReturnable<EnumActionResult> cir) {
        Item item = MC.player.inventory.getCurrentItem().getItem();
        //combat only items
        if(cir.getReturnValue() == EnumActionResult.PASS
                && !(item instanceof ItemBow)
                && (item instanceof ItemTool
                || item == Items.AIR
                || item == Items.TOTEM_OF_UNDYING
                || item instanceof ItemArrow
                || item instanceof ItemCompass
                || item instanceof ItemWrittenBook
                || item instanceof ItemGlassBottle
                || item instanceof ItemSaddle
                || item instanceof ItemSword)
        ) {
            Ares.EVENT_MANAGER.post(new OnItemUsePass(cir));
        }
    }
}
