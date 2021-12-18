package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.IWorld;
import dev.tigr.ares.fabric.utils.CopiedOtherClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

@SuppressWarnings("ConstantConditions")
public class CustomWorld implements IWorld {
    MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void setChunkCulling(boolean enabled) {
        MC.chunkCullingEnabled = enabled;
    }

    @Override
    public void removeEntity(int entity) {
        MC.world.removeEntity(entity, Entity.RemovalReason.DISCARDED);
    }

    @Override
    public int createAndSpawnClone() {
        CopiedOtherClientPlayerEntity entity = new CopiedOtherClientPlayerEntity(MC.world, MC.player);
        entity.copyFrom(MC.player);
        MC.world.addEntity(entity.getId(), entity);
        return entity.getId();
    }
}
