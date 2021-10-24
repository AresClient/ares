package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.player.InteractBlockEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumActionResult;

@Module.Info(name = "AntiGhostBlock", description = "Prevents ghost blocks by forcing packet-only placement", category = Category.MISC)
public class AntiGhostBlock extends Module {
    @EventHandler
    private final EventListener<InteractBlockEvent> onInteractWithBlock = new EventListener<>(event -> {
        if(!(event.player.getHeldItem(event.hand).getItem() instanceof ItemBlock)) return;

        if(!event.world.getWorldBorder().contains(event.pos)) {
            event.setReturnValue(EnumActionResult.FAIL);
        } else {
            float f = (float)(event.vec.x - (double)event.pos.getX());
            float f1 = (float)(event.vec.y - (double)event.pos.getY());
            float f2 = (float)(event.vec.z - (double)event.pos.getZ());
            event.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(event.pos, event.direction, event.hand, f, f1, f2));
            event.setReturnValue(EnumActionResult.SUCCESS);
        }

        event.setCancelled(true);
    });
}
