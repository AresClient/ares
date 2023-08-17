package org.aresclient.ares.mixin.math;

import org.aresclient.ares.api.minecraft.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.util.math.BlockPos.class)
public class MixinBlockPos extends MixinVec3i implements BlockPos {
}
