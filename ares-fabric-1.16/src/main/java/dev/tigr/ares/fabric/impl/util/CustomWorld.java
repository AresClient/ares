package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.IWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;

@SuppressWarnings("ConstantConditions")
public class CustomWorld implements IWorld {
    MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void setChunkCulling(boolean enabled) {
        MC.chunkCullingEnabled = enabled;
    }

    @Override
    public void removeEntity(int entity) {
        MC.world.removeEntity(entity);
    }

    @Override
    public int createAndSpawnClone() {
        OtherClientPlayerEntity entity = new OtherClientPlayerEntity(MC.world, MC.getSession().getProfile());
        entity.copyFrom(MC.player);
        MC.world.addEntity(entity.getEntityId(), entity);
        return entity.getEntityId();
    }
}
