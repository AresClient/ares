package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.class)
public interface AbstractBlockAccessor {
    @Accessor("slipperiness")
    void setSlipperiness(float slipperiness);
}
