package org.aresclient.ares.mixins;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.aresclient.ares.mixininterface.IEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityType.class)
public class MixinEntityType implements IEntityType {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(EntityType.EntityFactory factory, SpawnGroup spawnGroup, boolean saveable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet canSpawnInside, EntityDimensions dimensions, int maxTrackDistance, int trackTickInterval, FeatureSet requiredFeatures, CallbackInfo ci) {
        ordinal = pointer;
        pointer++;
    }

    private static int pointer = 0;
    int ordinal = -1;

    @Override
    public int ordinal() {
        return ordinal;
    }
}
