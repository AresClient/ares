package org.aresclient.ares.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

public class AccessMath {
    @Mixin(net.minecraft.util.math.Vec2f.class)
    public interface Vec2f {
        @Accessor("x") @Mutable void setX(float value);
        @Accessor("y") @Mutable void setY(float value);
    }

    @Mixin(net.minecraft.util.math.Vec3d.class)
    public interface Vec3d {
        @Accessor("x") @Mutable void setX(double value);
        @Accessor("y") @Mutable void setY(double value);
        @Accessor("z") @Mutable void setZ(double value);
    }
}
