package org.aresclient.ares.mixin.math;

import org.aresclient.ares.api.minecraft.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.util.math.Vec3d.class)
public class MixinVec3d implements Vec3d {
    @Mutable @Shadow @Final public double x;

    @Mutable @Shadow @Final public double y;

    @Mutable @Shadow @Final public double z;

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setX(double value) {
        x = value;
    }

    @Override
    public void setY(double value) {
        y = value;
    }

    @Override
    public void setZ(double value) {
        z = value;
    }
}
