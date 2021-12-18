package dev.tigr.ares.core.util.interfaces;

import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.doubles.V3D;

public interface IEntity {
    boolean isSelf(int entity);

    void copyFromTo(int from, int to);

    void setDead(int entity);

    default void addVelocity(int entity, V3D velocity) {
        addVelocity(entity, velocity.x, velocity.y, velocity.z);
    }

    default void addVelocity(int entity, V2D xzVelocity) {
        addVelocity(entity, xzVelocity.a, 0, xzVelocity.b);
    }

    void addVelocity(int entity, double x, double y, double z);

    default void setVelocity(int entity, V3D velocity) {
        setVelocity(entity, velocity.x, velocity.y, velocity.z);
    }

    void setVelocity(int entity, double x, double y, double z);
}
