package org.aresclient.ares.mixins.packet;

import org.aresclient.ares.api.packet.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.time.Instant;

@Mixin(net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket.class)
public class MixinChatMessageC2SPacket implements ChatMessageC2SPacket {
    @Mutable @Shadow @Final private String chatMessage;

    @Mutable
    @Shadow @Final private Instant timestamp;

    @Mutable
    @Shadow @Final private long salt;

    @Override
    public String getMessage() {
        return chatMessage;
    }

    @Override
    public void setMessage(String message) {
        chatMessage = message;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Instant value) {
        timestamp = value;
    }

    @Override
    public Long getSalt() {
        return salt;
    }

    @Override
    public void setSalt(long value) {
        salt = value;
    }
}
