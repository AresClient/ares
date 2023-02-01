package org.aresclient.ares.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import org.aresclient.ares.Ares;
import org.aresclient.ares.PacketEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConnection.class, priority = Integer.MAX_VALUE)
public class MixinNetworkConnection {
    @Inject(method = "send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    public void beforeSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if(Ares.Companion.getEVENT_MANAGER().post(
                new PacketEvent.Sent(packet, PacketEvent.Era.BEFORE)
        ).isCancelled())
            ci.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("RETURN"))
    public void afterSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        Ares.Companion.getEVENT_MANAGER().post(
                new PacketEvent.Sent(packet, PacketEvent.Era.BEFORE)
        );
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void beforeReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if(Ares.Companion.getEVENT_MANAGER().post(
                new PacketEvent.Sent(packet, PacketEvent.Era.BEFORE)
        ).isCancelled())
            ci.cancel();
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    public void afterReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        Ares.Companion.getEVENT_MANAGER().post(
                new PacketEvent.Received(packet, PacketEvent.Era.BEFORE)
        );
    }
}
