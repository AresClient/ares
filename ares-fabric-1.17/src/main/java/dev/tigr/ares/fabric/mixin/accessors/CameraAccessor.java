package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.render.Camera;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("area")
    BlockView getArea();
}
