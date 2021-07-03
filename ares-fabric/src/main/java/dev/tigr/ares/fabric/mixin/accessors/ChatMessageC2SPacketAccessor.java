package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatMessageC2SPacket.class)
public interface ChatMessageC2SPacketAccessor {
    @Accessor("chatMessage")
    void setChatMessage(String chatMessage);
}
