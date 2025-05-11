package org.aresclient.ares.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.Era;
import org.aresclient.ares.api.events.PacketEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection implements JWrapper {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("HEAD"), cancellable = true)
    private void preSendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if(EVENTS.post(new PacketEvent.Send(packet, Era.BEFORE)).isCancelled()) ci.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("TAIL"))
    private void postSendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        EVENTS.post(new PacketEvent.Send(packet, Era.AFTER));
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void preReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if(EVENTS.post(new PacketEvent.Receive(packet, Era.BEFORE)).isCancelled()) ci.cancel();
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("TAIL"))
    private void postReceivePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        EVENTS.post(new PacketEvent.Receive(packet, Era.AFTER));
    }
}
