package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@Mixin(MatrixStack.class)
public interface MatrixStackAccessor {
    @Accessor("stack")
    Deque<MatrixStack.Entry> getStack();
}
