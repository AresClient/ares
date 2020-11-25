package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.forge.event.events.client.NetworkExceptionEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // cancel chat packets with prefix in them
        if(packet instanceof CPacketChatMessage && ((CPacketChatMessage) packet).getMessage().startsWith(Command.PREFIX.getValue()))
            ci.cancel();

        if(Ares.EVENT_MANAGER.post(new PacketEvent.Sent(packet)).isCancelled()) ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new PacketEvent.Receive(packet)).isCancelled()) ci.cancel();
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    public void exception(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new NetworkExceptionEvent()).isCancelled()) ci.cancel();
    }
}
