package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.network.play.client.CPacketHeldItemChange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketHeldItemChange.class)
public interface CPacketHeldItemChangeAccessor {
    @Accessor("slotId")
    void setSlotId(int slot);
}
