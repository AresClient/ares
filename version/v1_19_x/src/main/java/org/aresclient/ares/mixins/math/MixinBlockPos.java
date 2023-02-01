package org.aresclient.ares.mixins.math;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockPos.class)
public class MixinBlockPos extends MixinVec3i implements org.aresclient.ares.api.math.BlockPos {
}
