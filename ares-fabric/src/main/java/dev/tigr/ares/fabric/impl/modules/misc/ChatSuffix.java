package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ChatSuffix", description = "Adds chatsuffix to end of every chat message", category = Category.MISC)
public class ChatSuffix extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof ChatMessageC2SPacket) {
            if(((ChatMessageC2SPacket) event.getPacket()).getChatMessage().startsWith("/") || ((ChatMessageC2SPacket) event.getPacket()).getChatMessage().startsWith("!"))
                return;

            ChatMessageC2SPacket chatPacket = (ChatMessageC2SPacket) event.getPacket();
            String plus = Ares.BRANCH == Ares.Branches.PLUS ? " \u1d18\u029f\u1d1c\ua731" : "";
            String msg = chatPacket.getChatMessage().concat(" \u00bb \u028c\u0433\u1d07\u0455" + plus);

            ReflectionHelper.setPrivateValue(ChatMessageC2SPacket.class, chatPacket, msg, "chatMessage", "field_12764");
        }
    });
}
