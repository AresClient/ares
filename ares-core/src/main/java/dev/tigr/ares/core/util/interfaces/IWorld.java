package dev.tigr.ares.core.util.interfaces;

public interface IWorld {
    void setChunkCulling(boolean enabled);

    void removeEntity(int entity);

    int createAndSpawnClone();
}
