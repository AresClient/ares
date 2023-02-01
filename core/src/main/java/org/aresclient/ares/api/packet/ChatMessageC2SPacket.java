package org.aresclient.ares.api.packet;

import org.aresclient.ares.Ares;

import java.time.Instant;

public interface ChatMessageC2SPacket {
    static ChatMessageC2SPacket create(String message, Instant timestamp, long salt) {
        return Ares.INSTANCE.creator.createCPacketChatMessage(message, timestamp, salt); //TODO: signature bs
    }

    String getMessage();
    void setMessage(String message);

    // 1.19+ Stuff

    Instant getTimestamp();
    void setTimestamp(Instant value);

    Long getSalt();
    void setSalt(long value);

    // TODO: 1.19 Signature
    // TODO: 1.19.1+ (Different)Signature + Acknowledgement
}
