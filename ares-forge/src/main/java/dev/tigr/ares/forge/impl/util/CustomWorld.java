package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.IWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;

@SuppressWarnings("ConstantConditions")
public class CustomWorld implements IWorld {
    Minecraft MC = Minecraft.getMinecraft();

    @Override
    public void setChunkCulling(boolean enabled) {
        MC.renderChunksMany = enabled;
    }

    @Override
    public void removeEntity(int entity) {
        MC.world.removeEntity(MC.world.getEntityByID(entity));
    }

    @Override
    public int createAndSpawnClone() {
        EntityOtherPlayerMP entity = new EntityOtherPlayerMP(MC.world, MC.getSession().getProfile());
        entity.copyLocationAndAnglesFrom(MC.player);
        MC.world.addEntityToWorld(entity.getEntityId(), entity);
        return entity.getEntityId();
    }
}
