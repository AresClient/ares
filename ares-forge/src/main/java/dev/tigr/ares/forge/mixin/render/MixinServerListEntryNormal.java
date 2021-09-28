package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.menu.MenuButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Makrennel 09/28/21
 */
@Mixin(ServerListEntryNormal.class)
public class MixinServerListEntryNormal {
    MenuButton replacement;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(GuiMultiplayer ownerIn, ServerData serverIn, CallbackInfo ci) {
        replacement = new MenuButton("");
    }

    @Inject(method = "drawEntry", at = @At("RETURN"))
    public void onEntryDrawn(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, CallbackInfo ci) {
        replacement.setX(x);
        replacement.setY(y);
        replacement.setHeight(slotHeight);
        replacement.setWidth(listWidth);
        if(ClickGUIMod.INSTANCE.customButtons.getValue()) replacement.draw(mouseX, mouseY, partialTicks);
    }
}
