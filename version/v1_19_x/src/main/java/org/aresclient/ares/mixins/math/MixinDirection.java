package org.aresclient.ares.mixins.math;

import net.minecraft.util.math.Direction;
import org.aresclient.ares.api.math.Facing;
import org.aresclient.ares.api.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Direction.class)
public class MixinDirection implements Facing {
    @Shadow @Final private int id;

    @Shadow @Final private net.minecraft.util.math.Vec3i vector;

    @Override
    public int getID() {
        return id;
    }

    @Override
    public Vec3i getOffset() {
        return (Vec3i) vector;
    }
}
