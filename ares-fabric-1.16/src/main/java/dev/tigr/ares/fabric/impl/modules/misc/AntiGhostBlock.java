package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.player.InteractBlockEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;

@Module.Info(name = "AntiGhostBlock", description = "Prevents ghost blocks by forcing packet-only placement", category = Category.MISC)
public class AntiGhostBlock extends Module {
    @EventHandler
    private final EventListener<InteractBlockEvent> onInteractWithBlock = new EventListener<>(event -> {
        if(!(event.player.getStackInHand(event.hand).getItem() instanceof BlockItem)) return;

        if(!event.world.getWorldBorder().contains(event.blockHitResult.getBlockPos())) {
            event.setReturnValue(ActionResult.FAIL);
        } else {
            event.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(event.hand, event.blockHitResult));
            event.setReturnValue(ActionResult.SUCCESS);
        }

        event.setCancelled(true);
    });
}
