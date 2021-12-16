package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.IEntity;
import net.minecraft.client.Minecraft;

@SuppressWarnings("ConstantConditions")
public class CustomEntity implements IEntity {
    Minecraft MC = Minecraft.getMinecraft();
    @Override
    public boolean isEntitySelf(int entity) {
        return MC.world.getEntityByID(entity) == MC.player;
    }
}
