package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.IEntity;
import net.minecraft.client.Minecraft;

@SuppressWarnings("ConstantConditions")
public class CustomEntity implements IEntity {
    Minecraft MC = Minecraft.getMinecraft();

    @Override
    public boolean isSelf(int entity) {
        if(MC.world == null || MC.player == null) return false;
        return MC.world.getEntityByID(entity) == MC.player;
    }

    @Override
    public void copyFromTo(int from, int to) {
        MC.world.getEntityByID(to).copyLocationAndAnglesFrom(MC.world.getEntityByID(from));
    }

    @Override
    public void setDead(int entity) {
        MC.world.getEntityByID(entity).setDead();
    }

    @Override
    public void addVelocity(int entity, double x, double y, double z) {
        MC.world.getEntityByID(entity).addVelocity(x, y, z);
    }

    @Override
    public void setVelocity(int entity, double x, double y, double z) {
        MC.world.getEntityByID(entity).setVelocity(x, y, z);
    }
}
