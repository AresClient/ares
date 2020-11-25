package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.optimizations.InfiniteChat;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * @author Tigermouthbear
 */
@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {
    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    public int maxChatLines(List<?> list) {
        if(Ares.EVENT_MANAGER.post(new InfiniteChat()).getResult() == Result.ALLOW) return -1;
        else return list.size();
    }
}
