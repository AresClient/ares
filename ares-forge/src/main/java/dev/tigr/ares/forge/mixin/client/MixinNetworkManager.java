package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.forge.event.events.client.NetworkExceptionEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.mixin.accessor.CPacketHeldItemChangeAccessor;
import dev.tigr.ares.forge.mixin.accessor.CPacketInputAccessor;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.tigr.ares.core.event.client.PacketEvent.Sent;

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

        // We have to post an event for each packet event that core uses
        if(packet instanceof CPacketHeldItemChange) {
            CPacketHeldItemChange p = (CPacketHeldItemChange) packet;
            Sent.HotbarSlot event = Ares.EVENT_MANAGER.post(new Sent.HotbarSlot(p.getSlotId()));
            if(event.isCancelled()) {
                ci.cancel();
                return;
            }
            if(event.getSlot() != p.getSlotId())
                ((CPacketHeldItemChangeAccessor) p).setSlotId(event.getSlot());
        }

        if(packet instanceof CPacketInput) {
            CPacketInput p = (CPacketInput) packet;
            Sent.Input event = Ares.EVENT_MANAGER.post(new Sent.Input(p.getForwardSpeed(), p.getStrafeSpeed(), p.isJumping(), p.isSneaking()));
            if(event.isCancelled()) {
                ci.cancel();
                return;
            }
            if(event.sideways != p.getStrafeSpeed()) ((CPacketInputAccessor) p).setSideways(event.sideways);
            if(event.forward != p.getForwardSpeed()) ((CPacketInputAccessor) p).setForward(event.forward);
            if(event.jumping != p.isJumping()) ((CPacketInputAccessor) p).setJumping(event.jumping);
            if(event.sneaking != p.isSneaking()) ((CPacketInputAccessor) p).setSneaking(event.sneaking);
        }
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
