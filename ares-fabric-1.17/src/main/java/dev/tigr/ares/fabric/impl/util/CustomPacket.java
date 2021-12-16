package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.IPacket;
import dev.tigr.ares.core.util.math.floats.V2F;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class CustomPacket implements IPacket {
    MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void hotbarSlotUpdate(int slot) {
        MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    @Override
    public void playerRotation(V2F rotation) {
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotation.a, rotation.b, MC.player.isOnGround()));
    }
}
