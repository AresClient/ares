package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("inNetherPortal")
    boolean isInNetherPortal();

    @Accessor("POSE")
    TrackedData<EntityPose> getPose();
}
