package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.IPacket;
import dev.tigr.ares.core.util.math.floats.V2F;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;

@SuppressWarnings("ConstantConditions")
public class CustomPacket implements IPacket {
    Minecraft MC = Minecraft.getMinecraft();

    @Override
    public void hotbarSlotUpdate(int slot) {
        MC.player.connection.sendPacket(new CPacketHeldItemChange(slot));
    }

    @Override
    public void playerRotation(V2F rotation) {
        MC.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.a, rotation.b, MC.player.onGround));
    }
}
