package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.IEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

@SuppressWarnings("ConstantConditions")
public class CustomEntity implements IEntity {
    MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public boolean isSelf(int entity) {
        if(MC.world == null || MC.player == null) return false;
        return MC.world.getEntityById(entity) == MC.player;
    }

    @Override
    public void copyFromTo(int from, int to) {
        MC.world.getEntityById(to).copyFrom(MC.world.getEntityById(from));
    }

    @Override
    public void setDead(int entity) {
        MC.world.getEntityById(entity).remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void addVelocity(int entity, double x, double y, double z) {
        MC.world.getEntityById(entity).addVelocity(x, y, z);
    }

    @Override
    public void setVelocity(int entity, double x, double y, double z) {
        MC.world.getEntityById(entity).setVelocity(x, y, z);
    }
}
