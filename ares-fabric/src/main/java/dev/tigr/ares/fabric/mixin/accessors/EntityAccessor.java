package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("inNetherPortal")
    boolean isInNetherPortal();

    @Accessor("POSE")
    TrackedData<EntityPose> getPose();

    @Invoker("setFlag")
    void setFlag(int index, boolean value);
}
