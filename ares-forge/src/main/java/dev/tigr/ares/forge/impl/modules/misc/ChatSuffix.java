package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ChatSuffix", description = "Adds chatsuffix to end of every chat message", category = Category.MISC)
public class ChatSuffix extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketChatMessage) {
            if(((CPacketChatMessage) event.getPacket()).getMessage().startsWith("/") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("!"))
                return;

            CPacketChatMessage chatPacket = (CPacketChatMessage) event.getPacket();
            String plus = Ares.BRANCH == Ares.Branches.PLUS ? " \u1d18\u029f\u1d1c\ua731" : "";
            String msg = chatPacket.getMessage().concat(" \u00bb \u028c\u0433\u1d07\u0455" + plus);

            ReflectionHelper.setPrivateValue(CPacketChatMessage.class, chatPacket, msg, "message", "field_149440_a");
        }
    });
}
