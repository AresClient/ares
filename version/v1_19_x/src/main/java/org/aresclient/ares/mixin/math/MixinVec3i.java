package org.aresclient.ares.mixin.math;

import org.aresclient.ares.api.minecraft.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.util.math.Vec3i.class)
public class MixinVec3i implements Vec3i {
    @Shadow private int x;

    @Shadow private int y;

    @Shadow private int z;

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void setX(int value) {
        x = value;
    }

    @Override
    public void setY(int value) {
        y = value;
    }

    @Override
    public void setZ(int value) {
        z = value;
    }
}
