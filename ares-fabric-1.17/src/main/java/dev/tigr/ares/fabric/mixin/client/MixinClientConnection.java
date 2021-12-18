package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerInputC2SPacketAccessor;
import dev.tigr.ares.fabric.mixin.accessors.UpdateSelectedSlotC2SPacketAccessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.tigr.ares.core.event.client.PacketEvent.Sent;

/**
 * @author Tigermouthbear 8/10/20
 */
@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        // execute command on send
        if(packet instanceof ChatMessageC2SPacket && ((ChatMessageC2SPacket) packet).getChatMessage().startsWith(Command.PREFIX.getValue())) {
            Command.execute(((ChatMessageC2SPacket) packet).getChatMessage());
            ci.cancel();
        }

        if(Ares.EVENT_MANAGER.post(new PacketEvent.Sent(packet)).isCancelled()) ci.cancel();

        // We have to post an event for each packet event that core uses
        if(packet instanceof UpdateSelectedSlotC2SPacket) {
            UpdateSelectedSlotC2SPacket p = (UpdateSelectedSlotC2SPacket) packet;
            Sent.HotbarSlot event = Ares.EVENT_MANAGER.post(new Sent.HotbarSlot(p.getSelectedSlot()));
            if(event.isCancelled()) {
                ci.cancel();
                return;
            }
            if(event.getSlot() != p.getSelectedSlot())
                ((UpdateSelectedSlotC2SPacketAccessor) p).setSelectedSlot(event.getSlot());
        }

        if(packet instanceof PlayerInputC2SPacket) {
            PlayerInputC2SPacket p = (PlayerInputC2SPacket) packet;
            Sent.Input event = Ares.EVENT_MANAGER.post(new Sent.Input(p.getForward(), p.getSideways(), p.isJumping(), p.isSneaking()));
            if(event.isCancelled()) {
                ci.cancel();
                return;
            }
            if(event.sideways != p.getSideways()) ((PlayerInputC2SPacketAccessor) p).setSideways(event.sideways);
            if(event.forward != p.getForward()) ((PlayerInputC2SPacketAccessor) p).setForward(event.forward);
            if(event.jumping != p.isJumping()) ((PlayerInputC2SPacketAccessor) p).setJumping(event.jumping);
            if(event.sneaking != p.isSneaking()) ((PlayerInputC2SPacketAccessor) p).setSneaking(event.sneaking);
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void receive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new PacketEvent.Receive(packet)).isCancelled()) ci.cancel();
    }
}
