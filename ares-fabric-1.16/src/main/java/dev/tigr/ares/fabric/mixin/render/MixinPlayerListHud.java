package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.ExtraTabEvent;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * @author Tigermouthbear 8/30/20
 */
@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    public List<PlayerListEntry> subList(List<PlayerListEntry> list, int fromIndex, int toIndex) {
        return list.subList(0, Math.min(list.size(), Ares.EVENT_MANAGER.post(new ExtraTabEvent()).getNum()));
    }
}
