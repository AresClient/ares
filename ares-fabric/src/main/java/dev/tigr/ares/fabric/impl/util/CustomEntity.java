package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.IEntity;
import net.minecraft.client.MinecraftClient;

@SuppressWarnings("ConstantConditions")
public class CustomEntity implements IEntity {
    MinecraftClient MC = MinecraftClient.getInstance();
    @Override
    public boolean isEntitySelf(int entity) {
        return MC.world.getEntityById(entity) == MC.player;
    }
}
