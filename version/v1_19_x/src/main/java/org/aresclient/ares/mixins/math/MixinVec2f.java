package org.aresclient.ares.mixins.math;

import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec2f.class)
public class MixinVec2f implements org.aresclient.ares.api.math.Vec2f {
    @Mutable @Shadow @Final public float x;
    @Mutable @Shadow @Final public float y;

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setX(float value) {
        x = value;
    }

    @Override
    public void setY(float value) {
        y = value;
    }
}
