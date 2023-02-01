package org.aresclient.ares.mixins.packet;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerMoveC2SPacket.OnGroundOnly.class)
public class MixinPlayerMoveC2SPacketOnGround extends MixinPlayerMoveC2SPacket {
}