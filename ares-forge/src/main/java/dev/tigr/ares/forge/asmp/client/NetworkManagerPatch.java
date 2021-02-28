package dev.tigr.ares.forge.asmp.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.forge.event.events.client.NetworkExceptionEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.asmp.annotations.At;
import dev.tigr.asmp.annotations.Patch;
import dev.tigr.asmp.annotations.modifications.Inject;
import dev.tigr.asmp.callback.CallbackInfo;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * provides hooks and patches for {@link net.minecraft.network.NetworkManager}
 * @author Tigermouthbear 2/28/21
 */
@Patch("net.minecraft.network.NetworkManager")
public class NetworkManagerPatch {
    @Inject(method = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    public void onSendPacket(CallbackInfo ci, Packet<?> packet) {
        // cancel chat packets with prefix in them
        if(packet instanceof CPacketChatMessage && ((CPacketChatMessage) packet).getMessage().startsWith(Command.PREFIX.getValue()))
            ci.cancel();

        if(Ares.EVENT_MANAGER.post(new PacketEvent.Sent(packet)).isCancelled()) ci.cancel();
    }

    @Inject(method = "Lnet/minecraft/network/NetworkManager;channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    public void onChannelRead(CallbackInfo ci, ChannelHandlerContext context, Packet<?> packet) {
        if(Ares.EVENT_MANAGER.post(new PacketEvent.Receive(packet)).isCancelled()) ci.cancel();
    }

    @Inject(method = "Lnet/minecraft/network/NetworkManager;exceptionCaught(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Throwable;)V", at = @At("HEAD"))
    public void exception(CallbackInfo ci, ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) {
        if(Ares.EVENT_MANAGER.post(new NetworkExceptionEvent()).isCancelled()) ci.cancel();
    }
}
