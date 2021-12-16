package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpdateSelectedSlotC2SPacket.class)
public interface UpdateSelectedSlotC2SPacketAccessor {
    @Mutable @Accessor("selectedSlot")
    void setSelectedSlot(int slot);
}
