package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.player.ExtraTabEvent;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * @author Tigermouthbear
 */
@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {
    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    public List<NetworkPlayerInfo> subList(List<NetworkPlayerInfo> list, int fromIndex, int toIndex) {
        return list.subList(0, Math.min(list.size(), Ares.EVENT_MANAGER.post(new ExtraTabEvent()).getNum()));
    }
}
