package org.aresclient.ares.mixins.math;

import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3i.class)
public class MixinVec3i implements org.aresclient.ares.api.math.Vec3i {
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
