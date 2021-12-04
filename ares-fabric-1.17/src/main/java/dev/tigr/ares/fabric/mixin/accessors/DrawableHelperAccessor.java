package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.gui.DrawableHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawableHelper.class)
public interface DrawableHelperAccessor {
    @Accessor("zOffset")
    int getZOffset();
}
